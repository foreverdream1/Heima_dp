package com.hmdp.mapper;

import com.hmdp.entity.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShopMapper {

    @Select("SELECT * FROM tb_shop WHERE id = #{id}")
    Shop findById(Long id);

    @Select("SELECT * FROM tb_shop WHERE type_id = #{typeId}")
    List<Shop> findByTypeId(Long typeId);

    @Select("SELECT * FROM tb_shop")
    List<Shop> findAll();
}
