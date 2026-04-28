package com.hmdp.mapper;

import com.hmdp.entity.Shop;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 店铺信息 Mapper 接口
 */
@Mapper
public interface ShopMapper {

    @Select("SELECT * FROM tb_shop WHERE id = #{id}")
    Shop findById(Long id);

    @Select("SELECT * FROM tb_shop WHERE type_id = #{typeId}")
    List<Shop> findByTypeId(Long typeId);

    @Select("SELECT * FROM tb_shop")
    List<Shop> findAll();

    /**
     * 插入店铺信息
     * @param shop 店铺实体
     * @return 影响的行数
     */
    @Insert("INSERT INTO tb_shop (name, type_id, images, area, address, x, y, avg_price, sold, comments, score, open_hours, create_time, update_time) " +
            "VALUES (#{name}, #{typeId}, #{images}, #{area}, #{address}, #{x}, #{y}, #{avgPrice}, #{sold}, #{comments}, #{score}, #{openHours}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Shop shop);

    /**
     * 更新店铺信息
     * @param shop 店铺实体
     * @return 影响的行数
     */
    @Update("UPDATE tb_shop SET name=#{name}, type_id=#{typeId}, images=#{images}, area=#{area}, address=#{address}, x=#{x}, y=#{y}, avg_price=#{avgPrice}, sold=#{sold}, comments=#{comments}, score=#{score}, open_hours=#{openHours}, update_time=#{updateTime} WHERE id=#{id}")
    int updateById(Shop shop);
}
