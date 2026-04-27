package com.hmdp.service.impl;

import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.IVoucherOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VoucherOrderServiceImpl implements IVoucherOrderService {

    @Resource
    private VoucherOrderMapper voucherOrderMapper;

    @Override
    public VoucherOrder findById(Long id) {
        return voucherOrderMapper.findById(id);
    }

    @Override
    public List<VoucherOrder> findByUserId(Long userId) {
        return voucherOrderMapper.findByUserId(userId);
    }
}
