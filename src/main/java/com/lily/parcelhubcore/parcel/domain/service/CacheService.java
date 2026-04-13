package com.lily.parcelhubcore.parcel.domain.service;

import java.time.Duration;

import jakarta.annotation.Resource;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
public class CacheService {

    @Resource
    private RedissonClient redissonClient;

    public String get(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    public void set(String key, String value) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    public void set(String key, String value, long hours) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(value, Duration.ofHours(hours));
    }

    public boolean delete(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.delete();
    }

}
