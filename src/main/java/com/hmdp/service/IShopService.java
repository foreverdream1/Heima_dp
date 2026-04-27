package com.hmdp.service;

import com.hmdp.entity.Shop;

import java.util.List;

public interface IShopService {
    Shop findById(Long id);
    List<Shop> findByTypeId(Long typeId);
}
