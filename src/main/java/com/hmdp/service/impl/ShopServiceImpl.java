package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;

import com.hmdp.utils.SystemConstants;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;

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
    public Result findByTypeId(Long typeId, Integer current, Double x, Double y) {
        //判断是否需要根据坐标查询
        if(x==null||y==null){
            //不需要坐标查询，直接分页查数据库
            int offset = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
            return Result.ok(shopMapper.findByTypeId(typeId, offset, SystemConstants.DEFAULT_PAGE_SIZE));
        }
        //计算分页参数
        int from =(current-1)* SystemConstants.DEFAULT_PAGE_SIZE;
        int end=current*SystemConstants.DEFAULT_PAGE_SIZE;
        //查询redis，按照距离排序，分页  结果：shopid，distanse         GEOSEARCH key BYLONLAT x y BYRADIUS 10 WITHDISTANCE
        String key=RedisConstants.SHOP_GEO_KEY+typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
                key,
                GeoReference.fromCoordinate(x, y), new Distance(5000),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
        );
        //解析id
        if(results==null){
            return Result.ok(Collections.emptyList());
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if(list.size()<=from){
            //没有下一页
            return Result.ok(Collections.emptyList());
        }
        //截取from-end
        List<Long> ids=new ArrayList<>(list.size());
        Map<String,Distance> distanceMap=new HashMap<>(list.size());
        list.stream().skip(from).forEach(r->{
            //获取店铺id
            String shopIdStr = r.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            //获取距离
            Distance distance = r.getDistance();
            distanceMap.put(shopIdStr,distance);
        });
        //查询shop
        List<Shop> shops=shopMapper.queryIdsAndOrderByDistance(ids,distanceMap);
        //返回
        for (Shop shop : shops) {
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        return Result.ok(shops);
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
