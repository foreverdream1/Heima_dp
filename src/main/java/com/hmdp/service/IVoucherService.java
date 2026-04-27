package com.hmdp.service;

import com.hmdp.entity.Voucher;

import java.util.List;

public interface IVoucherService {
    Voucher findById(Long id);
    List<Voucher> findAll();
    List<Voucher> queryVoucherOfShop(Long shopId);
}
