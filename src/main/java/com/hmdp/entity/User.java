package com.hmdp.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class User implements Serializable {
    private Long id;
    private String phone;
    private String password;
    private String nickName;
    private String icon = "";
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
