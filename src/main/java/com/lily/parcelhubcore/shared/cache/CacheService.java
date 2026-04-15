package com.lily.parcelhubcore.shared.cache;

import java.time.Duration;

import com.alibaba.fastjson2.JSON;
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

    /**
     * 存对象（转 JSON）
     */
    public <T> void set(String key, T value) {
        String jsonString = JSON.toJSONString(value);
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(jsonString);
    }

    /**
     * 存对象 + 过期时间（秒）
     */
    public <T> void set(String key, T value, long expireSeconds) {
        String jsonString = JSON.toJSONString(value);
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(jsonString, Duration.ofSeconds(expireSeconds));
    }

    /**
     * 取对象（JSON 转对象）
     */
    public <T> T get(String key, Class<T> clazz) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        String jsonString = bucket.get();
        if (jsonString == null) {
            return null;
        }
        return JSON.parseObject(jsonString, clazz);
    }

    public boolean delete(String key) {
        return redissonClient.getBucket(key).delete();
    }

}
