package com.hmdp.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BlogComments implements Serializable {
    private Long id;
    private Long userId;
    private Long blogId;
    private Long parentId;
    private Long answerId;
    private String content;
    private Integer liked;
    private Boolean status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
