package com.lily.parcelhubcore.shared.cache.config;

import com.lily.parcelhubcore.shared.lock.Lock;
import com.lily.parcelhubcore.shared.lock.RedissonDistributedLock;
import com.lily.parcelhubcore.shared.lock.config.DistributedLockProperties;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DistributedLockProperties.class)
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.database:0}") int database,
            @Value("${spring.data.redis.ssl.enabled:false}") boolean ssl
    ) {
        String protocol = ssl ? "rediss://" : "redis://";

        Config config = new Config();
        // 使用redis单机模式
        config.useSingleServer()
                .setAddress(protocol + host + ":" + port)
                .setDatabase(database);
        return Redisson.create(config);
    }

    @Bean
    public Lock lock(RedissonClient redissonClient,
                     DistributedLockProperties distributedLockProperties) {
        return new RedissonDistributedLock(
                redissonClient,
                distributedLockProperties.getPrefix()
        );
    }
}
