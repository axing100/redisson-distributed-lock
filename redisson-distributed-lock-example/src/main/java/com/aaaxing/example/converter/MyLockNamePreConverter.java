package com.aaaxing.example.converter;

import com.aaaxing.distributed.lock.converter.LockNamePreConverter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 自定义锁名预转换器
 * 如有特殊的锁名转换需求，实现LockNamePreConverter接口自定义锁名预转换逻辑
 *
 * @author axing
 * @date 2024-04-12
 */
@Component
public class MyLockNamePreConverter implements LockNamePreConverter {

    @Override
    public String preConvertLockName(String rawLockName, ProceedingJoinPoint joinPoint) {
        String lockName = rawLockName;

        lockName = convertClientIp(lockName);
        lockName = convertOther(lockName);
        return lockName;

    }

    /**
     * 转换客户端ip字段
     *
     * @param lockName
     * @return
     */
    private String convertClientIp(String lockName) {
        String clientIpMatch = "{@ip}";

        if (lockName.contains(clientIpMatch)) {
            String clientIp = null;
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            if (servletRequestAttributes != null) {
                HttpServletRequest request = servletRequestAttributes.getRequest();

                clientIp = request.getHeader("X-Forwarded-For");
                if (clientIp == null) {
                    clientIp = request.getRemoteAddr();
                }
            }
            lockName = lockName.replace(clientIpMatch, String.valueOf(clientIp));
        }
        return lockName;
    }

    /**
     * 转换其他
     *
     * @param lockName
     * @return
     */
    private String convertOther(String lockName) {
        // do other convert

        return lockName;
    }

}
