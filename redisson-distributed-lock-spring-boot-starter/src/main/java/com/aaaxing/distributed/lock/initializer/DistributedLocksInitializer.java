// Copyright 2024 axing
package com.aaaxing.distributed.lock.initializer;

import com.aaaxing.distributed.lock.config.DistributedLockProperties;
import com.aaaxing.distributed.lock.utils.DistributedLocks;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 分布式锁工具类初始化器
 *
 * @author axing
 * @date 2024-04-12
 */
@Component
public class DistributedLocksInitializer {
    @Resource
    private RedissonClient redisson;
    @Resource
    private DistributedLockProperties distributedLockProperties;

    @PostConstruct
    public void init() {
        DistributedLocks.setRedisson(redisson);
        DistributedLocks.setPrefix(distributedLockProperties.getPrefix());
    }
}
