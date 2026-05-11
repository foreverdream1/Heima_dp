package com.hmdp.service;

import com.hmdp.entity.SeckillVoucher;

public interface ISeckillVoucherService {
    SeckillVoucher findByVoucherId(Long voucherId);

    boolean updateStock(Long voucherId);
}
