// Copyright 2024 axing
package com.aaaxing.distributed.lock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分布式锁配置属性类
 *
 * @author axing
 * @date 2024-04-11
 */
@ConfigurationProperties(prefix = "distributed-lock")
public class DistributedLockProperties {

    /**
     * 锁名称前缀
     */
    private String prefix = "lock:";



    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
