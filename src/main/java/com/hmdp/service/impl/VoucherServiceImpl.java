package com.hmdp.service.impl;

import com.hmdp.entity.Voucher;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.service.IVoucherService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VoucherServiceImpl implements IVoucherService {

    @Resource
    private VoucherMapper voucherMapper;

    @Override
    public Voucher findById(Long id) {
        return voucherMapper.findById(id);
    }

    @Override
    public List<Voucher> findAll() {
        return voucherMapper.findAll();
    }

    @Override
    public List<Voucher> queryVoucherOfShop(Long shopId) {
        return voucherMapper.queryVoucherOfShop(shopId);
    }
}
