package com.hmdp.service.impl;

import com.hmdp.entity.SeckillVoucher;
import com.hmdp.mapper.SeckillVoucherMapper;
import com.hmdp.service.ISeckillVoucherService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class SeckillVoucherServiceImpl implements ISeckillVoucherService {

    @Resource
    private SeckillVoucherMapper seckillVoucherMapper;

    @Override
    public SeckillVoucher findByVoucherId(Long voucherId) {
        return seckillVoucherMapper.findByVoucherId(voucherId);
    }
}
