package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class VoucherOrderServiceImpl implements IVoucherOrderService {

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

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

//    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    private IVoucherOrderService proxy;

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    //    private class VoucherOrderHandler implements Runnable {
//
//        @Override
//        public void run() {
//            while (true) {
//                //获取队列中的订单信息
//                try {
//                    VoucherOrder voucherOrder = orderTasks.take();
//                    handlerVoucherOrder(voucherOrder);
//                } catch (InterruptedException e) {
//                    log.error("处理订单异常");
//                    throw new RuntimeException(e);
//                }
//
//            }
//        }
    private class VoucherOrderHandler implements Runnable {
        String queueName = "stream.orders";

        @Override
        public void run() {
            while (true) {
                try {
                    //获取消息队列中的信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000
                    List<MapRecord<String, Object, Object>> list = redisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );

                    //判断消息获取是否成功
                    if (list.isEmpty() || list == null) {
                        continue;
                    }
                    //解析消息中的订单信息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);//把获取的订单信息转为VoucherOrder对象保存，如果不存在，就忽略
                    //失败，没有消息，继续下一次循环

                    //如果成功，可以下单
                    handlerVoucherOrder(voucherOrder);
                    //ACK确认SACK stream.orders g1 c1
                    redisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());

                } catch (Exception e) {
                    log.error("处理订单异常");
                    handlePendingList();
                }
            }
        }

        private void handlePendingList() {
            while (true) {
                try {
                    //获取Pending-list队列中的信息 XREADGROUP GROUP g1 c1 COUNT 1
                    List<MapRecord<String, Object, Object>> list = redisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(queueName, ReadOffset.from("0"))
                    );

                    //判断消息获取是否成功
                    if (list.isEmpty() || list == null) {
                        //获取失败，说明pending-list没有异常消息，继续下一次循环
                        break;
                    }
                    //解析消息中的订单信息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);//把获取的订单信息转为VoucherOrder对象保存，如果不存在，就忽略
                    //失败，没有消息，继续下一次循环

                    //如果成功，可以下单
                    handlerVoucherOrder(voucherOrder);
                    //ACK确认SACK stream.orders g1 c1
                    redisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());

                } catch (Exception e) {
                    log.error("处理Pending-list异常");
                }
        }
    }

    private void handlerVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        RLock lock = null;
        try {
            //获取代理对象
            //创建锁对象
            lock = redissonClient.getLock("lock:order:" + userId);
            boolean isLock = lock.tryLock(1, 10, TimeUnit.SECONDS);
            if (!isLock) {
                //获取锁失败，返回错误或重试
                log.error("不允许重复下单");
                return;
            }

            proxy.getResult(voucherOrder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}

    @Override
    public Result seckillVoucher(Long voucherId) {
        //获取用户
        Long id = UserHolder.getUser().getId();
        //获取订单id
        long orderId = redisIdWorker.nextId("order");

        //执行lua脚本
        Long result = redisTemplate.execute(
                VoucherOrderServiceImpl.SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                id.toString(),
                String.valueOf(orderId)
        );
        //判断结果是否为0
        int r = result.intValue();
        //不为0，没有购买资格
        if (result != 0) {
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }

//        VoucherOrder voucherOrder = new VoucherOrder();
//        voucherOrder.setId(orderId);
//        voucherOrder.setUserId(id);
//        voucherOrder.setVoucherId(voucherId);
//
//
//        //为0，有购买资格，把下单信息保存到阻塞队列
//        orderTasks.add(voucherOrder);
    //获取代理对象
    proxy = (IVoucherOrderService) AopContext.currentProxy();
    //返回订单id
    return Result.ok(orderId);
}

//    @Override
//    public Result seckillVoucher(Long voucherId) {
//        //查询优惠券
//        SeckillVoucher voucher=seckillVoucherService.findByVoucherId(voucherId);
//        //判断秒杀是否开始
//        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
//            return Result.fail("秒杀尚未开始");
//        }
//        //判断秒杀是否结束
//        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
//            return Result.fail("秒杀已结束");
//        }
//        //判断库存是否充足
//        if (voucher.getStock()<1) {
//            return Result.fail("库存不足");
//        }
//
//        //扣减库存
//        boolean success=seckillVoucherService.updateStock(voucherId);
//        if(!success){
//            return Result.fail("库存不足");
//        }
////        SimpleRedisLock lock = null;
//        RLock lock=null;
//        try {
//            Long userId = UserHolder.getUser().getId();
////        synchronized (userId.toString().intern()) {//返回字符串规范模式
//            //获取代理对象
//            //创建锁对象
////            lock = new SimpleRedisLock("order:" + userId, redisTemplate);
//            lock = redissonClient.getLock("lock:order:" + userId);
//            boolean isLock = lock.tryLock(1,10, TimeUnit.SECONDS);
//            if(!isLock){
//                //获取锁失败，返回错误或重试
//                return Result.fail("不允许重复下单");
//            }
//            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
//            return proxy.getResult(voucherId,userId);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        finally{
//            lock.unlock();
//        }

/// /        }
//    }
@Transactional
public void getResult(VoucherOrder voucherOrder) {
    //一人一单
    Long userId = voucherOrder.getUserId();
    //查询订单
    int count = voucherOrderMapper.findByUserIdAndVoucherId(userId, voucherOrder);
    //判断是否购买过
    if (count > 0) {
        log.error("您已购买过该优惠券");
        return;
//            return Result.fail("您已购买过该优惠券");
    }
    boolean success = seckillVoucherService.updateStock(voucherOrder.getVoucherId());
    if (!success) {
        log.error("库存不足");
        return;
    }


//        //创建订单
//        VoucherOrder voucherOrder = new VoucherOrder();
//        //订单id
//        long orderId = redisIdWorker.nextId("order");
//        voucherOrder.setId(orderId);
//        //用户id
//
//        voucherOrder.setUserId(userId);
//        //代金券id
//        voucherOrder.setVoucherId(voucherOrder);
    voucherOrderMapper.save(voucherOrder);
//        //返回订单id
//        return Result.ok(voucherOrder);
}
}

