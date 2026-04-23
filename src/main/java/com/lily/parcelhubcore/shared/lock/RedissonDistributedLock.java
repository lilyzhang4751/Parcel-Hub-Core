package com.lily.parcelhubcore.shared.lock;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.util.StringUtils;

public class RedissonDistributedLock implements Lock {

    private final RedissonClient redissonClient;
    private final String lockPrefix;

    public RedissonDistributedLock(RedissonClient redissonClient, String lockPrefix) {
        this.redissonClient = Objects.requireNonNull(redissonClient, "redissonClient must not be null");
        this.lockPrefix = lockPrefix == null ? "" : lockPrefix;
    }

    @Override
    public boolean tryLock(String lockKey, long leaseTime, TimeUnit unit) {
        validate(lockKey, leaseTime, unit);

        RLock rLock = redissonClient.getLock(buildRealKey(lockKey));
        try {
            // waitTime = 0，表示立即尝试，不等待
            return rLock.tryLock(0L, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void unlock(String lockKey) {
        if (!StringUtils.hasText(lockKey)) {
            return;
        }

        RLock rLock = redissonClient.getLock(buildRealKey(lockKey));

        // Redisson 的 RLock 只能由持有锁的线程解锁；
        // 这里做保护，避免 finally 中误解锁抛异常
        if (rLock.isHeldByCurrentThread()) {
            rLock.unlock();
        }
    }

    private String buildRealKey(String lockKey) {
        return lockPrefix + lockKey;
    }

    private void validate(String lockKey, long leaseTime, TimeUnit unit) {
        if (!StringUtils.hasText(lockKey)) {
            throw new IllegalArgumentException("lockKey must not be blank");
        }
        if (leaseTime <= 0) {
            throw new IllegalArgumentException("leaseTime must be greater than 0");
        }
        if (unit == null) {
            throw new IllegalArgumentException("TimeUnit must not be null");
        }
    }
}
