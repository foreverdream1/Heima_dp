package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;

import java.util.List;

public interface IVoucherOrderService {
    VoucherOrder findById(Long id);
    List<VoucherOrder> findByUserId(Long userId);

    Result seckillVoucher(Long voucherId);

    Result getResult(Long voucherId, Long userId);
}
