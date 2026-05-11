package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.SimpleRedisLock;
import com.hmdp.utils.UserHolder;
import jakarta.annotation.Resource;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class VoucherOrderServiceImpl implements IVoucherOrderService {

    @Autowired
    private VoucherOrderMapper voucherOrderMapper;

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    @Autowired
    private RedisIdWorker redisIdWorker;

    @Override
    public VoucherOrder findById(Long id) {
        return voucherOrderMapper.findById(id);
    }

    @Override
    public List<VoucherOrder> findByUserId(Long userId) {
        return voucherOrderMapper.findByUserId(userId);
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        //查询优惠券
        SeckillVoucher voucher=seckillVoucherService.findByVoucherId(voucherId);
        //判断秒杀是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀尚未开始");
        }
        //判断秒杀是否结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已结束");
        }
        //判断库存是否充足
        if (voucher.getStock()<1) {
            return Result.fail("库存不足");
        }

        //扣减库存
        boolean success=seckillVoucherService.updateStock(voucherId);
        if(!success){
            return Result.fail("库存不足");
        }
//        SimpleRedisLock lock = null;
        RLock lock=null;
        try {
            Long userId = UserHolder.getUser().getId();
//        synchronized (userId.toString().intern()) {//返回字符串规范模式
            //获取代理对象
            //创建锁对象
//            lock = new SimpleRedisLock("order:" + userId, redisTemplate);
            lock = redissonClient.getLock("lock:order:" + userId);
            boolean isLock = lock.tryLock(1,10, TimeUnit.SECONDS);
            if(!isLock){
                //获取锁失败，返回错误或重试
                return Result.fail("不允许重复下单");
            }
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.getResult(voucherId,userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally{
            lock.unlock();
        }
//        }
    }

    @Transactional
    public @NonNull Result getResult(Long voucherId,Long userId) {
        //一人一单

        //查询订单
        int count=voucherOrderMapper.findByUserIdAndVoucherId(userId, voucherId);
        //判断是否购买过
        if(count>0){
            return Result.fail("您已购买过该优惠券");
        }


        //创建订单
        VoucherOrder voucherOrder=new VoucherOrder();
        //订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        //用户id

        voucherOrder.setUserId(userId);
        //代金券id
        voucherOrder.setVoucherId(voucherId);
        voucherOrderMapper.save(voucherOrder);
        //返回订单id
        return Result.ok(orderId);
        }
    }

