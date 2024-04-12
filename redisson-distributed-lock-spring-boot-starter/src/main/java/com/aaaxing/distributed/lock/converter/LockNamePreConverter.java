// Copyright 2024 axing
package com.aaaxing.distributed.lock.converter;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 锁名预转换器
 *
 * @author axing
 * @date 2024-04-11
 */
@FunctionalInterface
public interface LockNamePreConverter {

    /**
     * 前置转换锁名称
     *
     * <br>可以实现此接口方法来转换特殊值
     *
     * @param rawLockName 原始锁名称，包含{field}占位符
     * @param joinPoint
     * @return 进行前置转换后的锁名称
     */
    String preConvertLockName(String rawLockName, ProceedingJoinPoint joinPoint);
}
