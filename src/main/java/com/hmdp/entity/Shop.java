package com.hmdp.entity;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺实体类
 * 对应数据库表 tb_shop
 */
@Data
public class Shop implements Serializable {
    /** 主键ID */
    private Long id;
    
    /** 店铺名称 */
    @NotBlank(message = "店铺名称不能为空")
    private String name;
    
    /** 店铺类型ID */
    @NotNull(message = "店铺类型不能为空")
    private Long typeId;
    
    /** 店铺图片（JSON数组格式） */
    private String images;
    
    /** 所在区域 */
    private String area;
    
    /** 详细地址 */
    private String address;
    
    /** 经度 */
    private Double x;
    
    /** 纬度 */
    private Double y;
    
    /** 人均价格（单位：分） */
    private Long avgPrice;
    
    /** 已售数量 */
    private Integer sold;
    
    /** 评论数量 */
    private Integer comments;
    
    /** 评分（1-5分） */
    private Integer score;
    
    /** 营业时间 */
    private String openHours;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /** 距离（临时字段，不持久化） */
    private transient Double distance;
}
