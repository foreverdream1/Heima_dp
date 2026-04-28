package com.hmdp.service;

import com.hmdp.entity.ShopType;

import java.util.List;

public interface IShopTypeService {
    /**
     * 分类查询
     * @return
     */
    List<ShopType> findAll();
}
