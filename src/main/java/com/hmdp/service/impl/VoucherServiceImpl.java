package com.hmdp.service.impl;

import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.Voucher;
import com.hmdp.mapper.SeckillVoucherMapper;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.IVoucherService;
import com.hmdp.utils.RedisConstants;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VoucherServiceImpl implements IVoucherService {

    @Autowired
    private VoucherMapper voucherMapper;

    @Autowired
    private VoucherOrderMapper voucherOrderMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SeckillVoucherMapper seckillVoucherMapper;

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

    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        //保存优惠券
        voucherMapper.save(voucher);
        //保存秒杀优惠券
        SeckillVoucher seckillVoucher=new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherMapper.save(seckillVoucher);
        //保存秒杀库存
        redisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY +voucher.getId(),voucher.getStock().toString());
    }
}
