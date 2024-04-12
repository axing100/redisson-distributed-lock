// Copyright 2024 axing
package com.aaaxing.distributed.lock.config;

import com.aaaxing.distributed.lock.aspect.DistributedLockAspect;
import com.aaaxing.distributed.lock.converter.DefaultLockNamePreConverter;
import com.aaaxing.distributed.lock.converter.LockNameCoreConverter;
import com.aaaxing.distributed.lock.converter.LockNamePreConverter;
import com.aaaxing.distributed.lock.initializer.DistributedLocksInitializer;
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
    public LockNameCoreConverter lockNameCoreConverter(DistributedLockProperties distributedLockProperties,
                                                       LockNamePreConverter lockNamePreConverter) {
        return new LockNameCoreConverter(distributedLockProperties, lockNamePreConverter);
    }

    @Bean
    public DistributedLockAspect distributedLockAspect(LockNameCoreConverter lockNameCoreConverter) {
        return new DistributedLockAspect(lockNameCoreConverter);
    }

    @Bean
    public DistributedLocksInitializer distributedLocksInitializer() {
        return new DistributedLocksInitializer();
    }
}