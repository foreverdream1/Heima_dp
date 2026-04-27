package com.hmdp.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Follow implements Serializable {
    private Long id;
    private Long userId;
    private Long followUserId;
    private LocalDateTime createTime;
}
