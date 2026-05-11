package com.hmdp.utils;

public interface ILock {
    /**
     * 加锁
     * @param timeout 超时时间
     * @return 是否加锁成功
     */
    boolean lock( long timeout);

    /**
     * 解锁
     */
    void unlock();
}
