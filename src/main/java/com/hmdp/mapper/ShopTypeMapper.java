package com.hmdp.mapper;

import com.hmdp.entity.ShopType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShopTypeMapper {

    @Select("SELECT * FROM tb_shop_type ORDER BY sort")
    List<ShopType> findAll();
}
