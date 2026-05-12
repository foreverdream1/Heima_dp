---
--- Created by 16380
--- DateTime: 2026/5/11 23:09
---
-- 优惠券id
local voucherId = ARGV[1]
-- 用户id
local userId = ARGV[2]
-- 订单id（由Java层生成后传入）
local orderId = ARGV[3]
-- 库存key
local stockKey = 'seckill:stock:' .. voucherId
-- 订单key
local orderKey = 'seckill:order:' .. voucherId

-- 脚本业务
-- 判断库存是否充足
if (tonumber(redis.call('get', stockKey)) <= 0) then
    -- 库存不足返回1
    return 1
end
-- 判断用户是否已下单（一人一单）
if (redis.call('sismember', orderKey, userId) == 1) then
    -- 用户已下单返回2
    return 2
end
-- 扣库存
redis.call('incrby', stockKey, -1)
-- 记录下单用户
redis.call('sadd', orderKey, userId)
-- 发送消息到 Stream 队列（异步处理订单落库）
redis.call('xadd', 'stream.orders', '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)
return 0
