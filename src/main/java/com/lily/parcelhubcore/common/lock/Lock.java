package com.lily.parcelhubcore.common.lock;

import java.util.concurrent.TimeUnit;

public interface Lock {

    /**
     * 尝试获取锁
     *
     * @param lockKey   业务锁 key
     * @param leaseTime 锁自动释放时间
     * @param unit      时间单位
     * @return true=加锁成功，false=加锁失败
     */
    boolean tryLock(String lockKey, long leaseTime, TimeUnit unit);

    /**
     * 释放锁
     *
     * @param lockKey 业务锁 key
     */
    void unlock(String lockKey);
}
