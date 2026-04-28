package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;

import java.util.List;

public interface IShopService {
    /**
     * 根据id查询商户信息
     * @param id
     * @return
     */
    Result findById(Long id);

    /**
     * 根据商户类型查询商户信息
     * @param typeId
     * @return
     */
    List<Shop> findByTypeId(Long typeId);

    /**
     * 根据id更新商户
     * @param shop
     * @return
     */
    Result updateById(Shop shop);

    /**
     * 保存店铺信息
     * @param shop 店铺实体
     * @return 操作结果
     */
    Result save(Shop shop);
}
