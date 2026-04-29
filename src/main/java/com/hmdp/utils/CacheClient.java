package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.hmdp.utils.RedisConstants;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class CacheClient {


    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ShopMapper shopMapper;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,unit);
    }

    public void setWithLogicalExpire(String key,Object value,Long time,TimeUnit unit){
        //设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        //写入redis
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(redisData));
    }
    public <R,ID> R queryWithThrough(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallback,Long time,TimeUnit unit){
        String key=keyPrefix+id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(shopJson)){
            return JSONUtil.toBean(shopJson,type);
        }
        if(shopJson!=null){
            return null;
        }
        R r =dbFallback.apply(id);

        if(r==null){
            stringRedisTemplate.opsForValue().set(key,"",RedisConstants.CACHE_NULL_TTL,TimeUnit.MINUTES);
            return null;
        }
        this.set(key,r,time,unit);
        return r;
    }



    public <R,ID> R queryWithLogicalExpire(String keyPrefix,ID id,Class<R> type,Function<ID,R> dbFallback,Long time,TimeUnit unit) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        // 1. 从缓存查询
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isBlank(shopJson)) {
            return null;
        }
        //命中需要判断时间
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        R r=JSONUtil.toBean((JSONObject) redisData.getData(),type);
        LocalDateTime expireTime = redisData.getExpireTime();
        //判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            //未过期，直接返回数据
            return r;
        }

        //已过期，需要缓存重建

//        获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean islock = tryLock(lockKey);
        //判断是否获取锁成功

        if (islock) {
            //获取锁成功，需要重新查询数据库
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                //重建缓存
                try {
                    //查数据库
                    R r1 = dbFallback.apply(id);
                    //写入redis
                    this.setWithLogicalExpire(key,r1,time,unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
//                成功开启线程重建缓存
                finally {
                    unLock(lockKey);
                }
            });
        }
        //失败直接返回数据（过期）

        return r;
    }

    public Shop queryWithMutexLock(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        // 1. 从缓存查询
        boolean[] isNullHit = new boolean[1];
        Shop shop = getFromCache(key, isNullHit);
        if (shop != null) {
            return shop; // 缓存命中店铺
        }
        if (isNullHit[0]) {
            // 命中空值缓存，直接返回null
            return null;
        }
        // 2. 缓存未命中，尝试获取锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean locked = false;
        try {
            // 尝试获取锁，最多重试3次
            for (int i = 0; i < 3; i++) {
                locked = tryLock(lockKey);
                if (locked) {
                    break;
                }
                Thread.sleep(50); // 休眠后重试
            }
            if (!locked) {
                // 重试后仍未获取到锁，直接查询数据库（避免长时间等待）
                shop = queryFromDb(id);
                if (shop == null) {
                    // 查询为空，可以设置空值缓存，但未获取锁，可能存在并发问题，暂时不设置
                    // 因为其他线程可能正在更新缓存
                }
                return shop;
            }
            // 3. 获取锁成功，再次检查缓存（双重检查）
            shop = getFromCache(key, isNullHit);
            if (shop != null) {
                return shop; // 缓存命中店铺
            }
            if (isNullHit[0]) {
                // 命中空值缓存，直接返回null
                return null;
            }
            // 4. 查询数据库
            shop = queryFromDb(id);
            if (shop == null) {
                setNullCache(key);
                return null;
            }
            // 5. 写入缓存
            setCache(key, shop);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (locked) {
                unLock(lockKey);
            }
        }
        return shop;
    }


    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1",
                RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

    // ==================== 辅助方法 ====================

    /**
     * 从缓存中查询店铺信息
     *
     * @param key       缓存key
     * @param isNullHit 输出参数，如果命中空值缓存则设置为true，否则false
     * @return 店铺对象（如果缓存命中且非空），null表示缓存未命中或空值命中
     */
    private Shop getFromCache(String key, boolean[] isNullHit) {
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(shopJson)) {
            isNullHit[0] = false;
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        // 命中空值
        if (shopJson != null) {
            isNullHit[0] = true;
            return null;
        }
        // 缓存未命中
        isNullHit[0] = false;
        return null;
    }

    /**
     * 从数据库查询店铺
     */
    private Shop queryFromDb(Long id) {
        return shopMapper.findById(id);
    }

    /**
     * 设置空值缓存（解决缓存穿透）
     */
    private void setNullCache(String key) {
        stringRedisTemplate.opsForValue().set(key, "",
                RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
    }

    /**
     * 设置缓存
     */
    private void setCache(String key, Shop shop) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop),
                RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
    }
}
