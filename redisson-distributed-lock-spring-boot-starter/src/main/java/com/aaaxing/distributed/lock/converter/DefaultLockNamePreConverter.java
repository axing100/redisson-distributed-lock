// Copyright 2024 axing
package com.aaaxing.distributed.lock.converter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 默认锁名预转换器
 *
 * @author axing
 * @date 2024-04-11
 */
public final class DefaultLockNamePreConverter implements LockNamePreConverter {

    /**
     * 前置转换锁名称
     *
     * <br>如下代码将{ @userId}转换为从httpRequest获取到的登录用户id（自定义权限过滤器设置的值）
     *
     * @param rawLockName 原始锁名称，包含{field}占位符
     * @param joinPoint
     * @return 进行前置转换后的锁名称
     */
    @Override
    public String preConvertLockName(String rawLockName, ProceedingJoinPoint joinPoint) {
        String lockName = rawLockName;
        String userIdMatch = "{@userId}";
        String userIdAttribute = "userId";

        if (lockName.contains(userIdMatch)) {
            Long userId = null;
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            if (servletRequestAttributes != null) {
                HttpServletRequest request = servletRequestAttributes.getRequest();

                userId = (Long) request.getAttribute(userIdAttribute);
            }
            lockName = lockName.replace(userIdMatch, String.valueOf(userId));
        }
        return lockName;
    }
}
