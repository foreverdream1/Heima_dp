package com.hmdp.entity;

import com.hmdp.annotation.AutoFill;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class VoucherOrder implements Serializable {
    private Long id;
    private Long userId;
    private Long voucherId;
    private Integer payType;
    private Integer status;
    @AutoFill
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime useTime;
    private LocalDateTime refundTime;
    @AutoFill
    private LocalDateTime updateTime;
}
