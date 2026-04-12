package com.lily.parcelhubcore.application.service.impl;

import com.lily.parcelhubcore.common.lock.Lock;
import com.lily.parcelhubcore.application.service.ParcelOpService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ParcelOpServiceImpl implements ParcelOpService {

    @Resource
    private Lock lock;

    @Override
    public void inbound(Long userId, String orderNo) {
        // 业务 key：建议由业务唯一维度拼接
        String lockKey = "order:create:" + userId + ":" + orderNo;

        boolean locked = lock.tryLock(lockKey, 30, TimeUnit.SECONDS);
        if (!locked) {
            throw new IllegalStateException("当前请求过于频繁，未获取到分布式锁");
        }

        try {
            // =========================
            // 这里写你的核心业务逻辑
            // =========================
            System.out.println("执行业务逻辑: userId=" + userId + ", orderNo=" + orderNo);

            // 模拟业务耗时
            // Thread.sleep(2000);

        } finally {
            lock.unlock(lockKey);
        }
    }
}
