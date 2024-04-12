// Copyright 2024 axing
package com.aaaxing.distributed.lock.exception;

/**
 * 分布式锁异常
 *
 * @author axing
 * @date 2024-04-10
 */
public class DistributedLockException extends RuntimeException {
    /**
     * 状态码 0注解参数异常 1依赖异常 2转换锁名称异常 3加锁异常 4解锁异常 5 tryLock获取锁失败
     */
    private final Integer status;

    public DistributedLockException(Integer status, String message) {
        super(message);
        this.status = status;
    }

    public DistributedLockException(Integer status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
