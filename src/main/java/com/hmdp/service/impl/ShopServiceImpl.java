package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
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

    @Override
    public Result findById(Long id) {

        String key= RedisConstants.CACHE_SHOP_KEY +id;
        //1.根据id从redis查询商户信息
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2.如果商户信息不存在，抛出异常
        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        //3.如果商户信息存在，返回商户信息
        Shop shop=shopMapper.findById(id);
        //4.如果商户信息不存在，查询数据库，将商户信息保存到redis
        if(shop==null){
            return Result.fail("商户不存在");
        }
        //5.返回商户信息
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        return Result.ok(shop);
    }

    @Override
    public List<Shop> findByTypeId(Long typeId) {
        return shopMapper.findByTypeId(typeId);
    }

    @Override
    @Transactional
    public Result updateById(Shop shop) {
        Long id = shop.getId();
        if(id==null){
            return Result.fail("店铺id不能为空");
        }

        //更新数据
        shopMapper.updateById(shop);
        //删除缓存
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY+ id);
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
