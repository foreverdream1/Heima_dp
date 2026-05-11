

-- 比较锁标识，一致才释放（原子操作，防止误删他人的锁）
if (redis.call('get', KEYS[1]) == ARGV[1]) then
    -- 释放锁 del key
    return redis.call('del', KEYS[1])
end
return 0
