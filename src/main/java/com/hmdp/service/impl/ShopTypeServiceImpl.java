package com.hmdp.service.impl;

import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopTypeServiceImpl implements IShopTypeService {

    @Autowired
    private ShopTypeMapper shopTypeMapper;

    @Override
    public List<ShopType> findAll() {
        return shopTypeMapper.findAll();
    }
}
