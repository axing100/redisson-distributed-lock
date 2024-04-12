// Copyright 2024 axing
package com.aaaxing.distributed.lock.converter;

import com.aaaxing.distributed.lock.config.DistributedLockProperties;
import com.aaaxing.distributed.lock.exception.DistributedLockException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 锁名核心转换器
 *
 * @author axing
 * @date 2024-04-11
 */
@Component
public class LockNameCoreConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockNameCoreConverter.class);
    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)}");
    private final DistributedLockProperties distributedLockProperties;
    private final LockNamePreConverter lockNamePreConverter;

    public LockNameCoreConverter(DistributedLockProperties properties, LockNamePreConverter lockNamePreConverter) {
        this.distributedLockProperties = properties;
        this.lockNamePreConverter = lockNamePreConverter;
    }


    /**
     * 转换锁名称
     *
     * @param rawLockName 原始锁名称，包含{field}占位符
     * @param joinPoint
     * @return 替换占位符后的名称
     */
    public String convertLockName(String rawLockName, ProceedingJoinPoint joinPoint) {

        String lockName;

        try {
            lockName = lockNamePreConverter.preConvertLockName(rawLockName, joinPoint);
            Objects.requireNonNull(lockName);
        } catch (Exception e) {
            throw new DistributedLockException(2, "An exception occurred while pre convert lock name, " +
                    "please check the LockNamePreConverter.preConvertLockName method.", e.getCause());
        }

        try {
            String prefix = distributedLockProperties.getPrefix();
            if (StringUtils.isNotBlank(prefix)) {
                lockName = prefix + lockName;
            }
            HashMap<String, Object> paramMap = null;
            Object tempObj = null;

            Matcher matcher = PATTERN.matcher(lockName);
            while (matcher.find()) {
                if (paramMap == null) {
                    String[] paramNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
                    Object[] args = joinPoint.getArgs();

                    paramMap = new HashMap<>((int) Math.ceil(paramNames.length / 0.75));

                    for (int i = 0; i < paramNames.length; i++) {
                        if (!(args[i] instanceof HttpServletRequest || args[i] instanceof HttpServletResponse)) {
                            paramMap.put(paramNames[i], args[i]);
                            tempObj = args[i];
                        }
                    }
                }
                // 匹配整个大括号及其内容
                String match = matcher.group();
                // 仅匹配大括号内的内容
                String field = matcher.group(1);

                if (!field.contains(".")) {
                    if (paramMap.containsKey(field)) {
                        Object o = paramMap.get(field);
                        String fieldValue = String.valueOf(o);
                        lockName = lockName.replace(match, fieldValue);
                    } else if (paramMap.size() == 1){
                        Object o = tempObj == null ? null : getFieldValue(tempObj, field);
                        String fieldValue = String.valueOf(o);
                        lockName = lockName.replace(match, fieldValue);
                    } else {
                        lockName = lockName.replace(match, "null");
                    }
                } else {
                    String[] split = field.split("\\.");
                    Object o = paramMap.get(split[0]);
                    for (int i = 1; i < split.length; i++) {
                        if (o == null) {
                            break;
                        }
                        o = getFieldValue(o, split[i]);
                    }
                    String fieldValue = String.valueOf(o);
                    lockName = lockName.replace(match, fieldValue);
                }
            }
        } catch (Exception e) {
            throw new DistributedLockException(2, "An exception occurred while convert lock name.", e.getCause());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Lock name converted: {} -> {}", rawLockName, lockName);
        }
        return lockName;
    }

    /**
     * 获取对象的字段值
     *
     * @param obj 对象
     * @param field 字段名
     * @return 字段值
     */
    private Object getFieldValue(Object obj, String field) {
        Object value = null;
        try {
            value = FieldUtils.readDeclaredField(obj, field,true);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Failed to get field[{}] value for obj[{}].", field, obj, e);
            }
        }
        return value;
    }
}
