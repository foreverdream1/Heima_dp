package com.hmdp.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Blog implements Serializable {
    private Long id;
    private Long shopId;
    private Long userId;
    private transient String icon;
    private transient String name;
    private transient Boolean isLike;
    private String title;
    private String images;
    private String content;
    private Integer liked;
    private Integer comments;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
