package com.hmdp.service.impl;

import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopServiceImpl implements IShopService {

    @Resource
    private ShopMapper shopMapper;

    @Override
    public Shop findById(Long id) {
        return shopMapper.findById(id);
    }

    @Override
    public List<Shop> findByTypeId(Long typeId) {
        return shopMapper.findByTypeId(typeId);
    }
}
