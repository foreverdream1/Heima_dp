package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

import java.util.concurrent.TimeUnit;

@Service
public class ShopServiceImpl implements IShopService {

    @Autowired
    private ShopMapper shopMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Override
    public Result findById(Long id) {
        //缓存穿透
        //互斥锁
        //逻辑过期


        Shop shop = cacheClient.queryWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY, id, Shop.class, this::getById, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);

    }

    @Override
    public List<Shop> findByTypeId(Long typeId) {
        return shopMapper.findByTypeId(typeId);
    }

    /**
     * 根据ID从数据库查询店铺信息（不涉及缓存）
     *
     * @param id 店铺ID
     * @return 店铺对象，如果不存在则返回null
     */
    private Shop getById(Long id) {
        return shopMapper.findById(id);
    }

    @Override
    @Transactional
    public Result updateById(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }

        //更新数据
        shopMapper.updateById(shop);
        //删除缓存
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result save(Shop shop) {
        // 参数校验
        if (shop == null) {
            return Result.fail("店铺信息不能为空");
        }
        if (shop.getName() == null || shop.getName().trim().isEmpty()) {
            return Result.fail("店铺名称不能为空");
        }
        if (shop.getTypeId() == null) {
            return Result.fail("店铺类型不能为空");
        }

        // 设置创建时间和更新时间
        shop.setCreateTime(java.time.LocalDateTime.now());
        shop.setUpdateTime(java.time.LocalDateTime.now());

        // 保存到数据库
        int result = shopMapper.insert(shop);
        if (result <= 0) {
            return Result.fail("店铺保存失败");
        }

        return Result.ok("店铺保存成功");
    }
}
