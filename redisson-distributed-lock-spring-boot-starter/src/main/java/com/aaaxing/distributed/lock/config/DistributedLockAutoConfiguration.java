// Copyright 2024 axing
package com.aaaxing.distributed.lock.config;

import com.aaaxing.distributed.lock.aspect.DistributedLockAspect;
import com.aaaxing.distributed.lock.converter.DefaultLockNamePreConverter;
import com.aaaxing.distributed.lock.converter.LockNameCoreConverter;
import com.aaaxing.distributed.lock.converter.LockNamePreConverter;
import com.aaaxing.distributed.lock.utils.DistributedLocks;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式锁自动装配类
 * @author axing
 * @date 2024-04-10
 */
@Configuration
@AutoConfigureAfter(RedissonAutoConfiguration.class)
@EnableConfigurationProperties(DistributedLockProperties.class)
public class DistributedLockAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LockNamePreConverter lockNamePreConverter() {
        return new DefaultLockNamePreConverter();
    }

    @Bean
    public LockNameCoreConverter lockNameCoreConverter(DistributedLockProperties properties,
                                                       LockNamePreConverter lockNamePreConverter) {
        return new LockNameCoreConverter(properties, lockNamePreConverter);
    }

    @Bean
    public DistributedLockAspect distributedLockAspect(LockNameCoreConverter lockNameCoreConverter) {
        return new DistributedLockAspect(lockNameCoreConverter);
    }

    @Bean
    public DistributedLocks distributedLocks(DistributedLockProperties distributedLockProperties,
                                             RedissonClient redisson) {
        DistributedLocks.setRedisson(redisson);
        DistributedLocks.setPrefix(distributedLockProperties.getPrefix());
        return new DistributedLocks();
    }
}