package com.hmdp.mapper;

import com.hmdp.entity.Shop;
import org.apache.ibatis.annotations.*;
import org.springframework.data.geo.Distance;

import java.util.List;
import java.util.Map;

/**
 * 店铺信息 Mapper 接口
 */
@Mapper
public interface ShopMapper {

    @Select("SELECT * FROM tb_shop WHERE id = #{id}")
    Shop findById(Long id);

    /**
     * 按类型分页查询店铺（无坐标路径，走 XML）
     *
     * @param typeId   店铺类型 id
     * @param offset   跳过的记录数 (current-1)*pageSize
     * @param pageSize 每页条数
     */
    List<Shop> findByTypeId(@Param("typeId") Long typeId,
                            @Param("offset") int offset,
                            @Param("pageSize") int pageSize);

    @Select("SELECT * FROM tb_shop")
    List<Shop> findAll();

    /**
     * 插入店铺信息
     */
    @Insert("INSERT INTO tb_shop (name, type_id, images, area, address, x, y, avg_price, sold, comments, score, open_hours, create_time, update_time) " +
            "VALUES (#{name}, #{typeId}, #{images}, #{area}, #{address}, #{x}, #{y}, #{avgPrice}, #{sold}, #{comments}, #{score}, #{openHours}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Shop shop);

    /**
     * 更新店铺信息
     */
    @Update("UPDATE tb_shop SET name=#{name}, type_id=#{typeId}, images=#{images}, area=#{area}, address=#{address}, x=#{x}, y=#{y}, avg_price=#{avgPrice}, sold=#{sold}, comments=#{comments}, score=#{score}, open_hours=#{openHours}, update_time=#{updateTime} WHERE id=#{id}")
    int updateById(Shop shop);

    /**
     * 按 id 列表查询店铺，并按 ids 顺序（即 distance 升序）返回（走 XML）
     *
     * @param ids         Redis GEO 返回的按距离排序的店铺 id 列表
     * @param distanceMap 店铺 id → Distance 映射（XML 中不使用，Service 层在结果中回填 distance 字段）
     */
    List<Shop> queryIdsAndOrderByDistance(@Param("ids") List<Long> ids,
                                          @Param("distanceMap") Map<String, Distance> distanceMap);
}
