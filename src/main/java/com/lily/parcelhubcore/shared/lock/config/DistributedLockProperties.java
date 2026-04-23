package com.lily.parcelhubcore.shared.lock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.distributed-lock")
public class DistributedLockProperties {
    /**
     * Redis 中锁 key 的统一前缀，避免和别的业务 key 冲突
     */
    private String prefix = "parcel:core:lock:";

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
