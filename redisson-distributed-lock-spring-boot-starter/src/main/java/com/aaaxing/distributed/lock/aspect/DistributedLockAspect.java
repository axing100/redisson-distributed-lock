// Copyright 2024 axing
package com.aaaxing.distributed.lock.aspect;

import com.aaaxing.distributed.lock.annotation.DistributedLock;
import com.aaaxing.distributed.lock.converter.LockNameCoreConverter;
import com.aaaxing.distributed.lock.exception.DistributedLockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.aaaxing.distributed.lock.annotation.DistributedLock.*;


/**
 * 分布式锁切面
 *
 * @author axing
 * @date 2024-04-09
 */
@Aspect
@Component
public class DistributedLockAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedLockAspect.class);
    private final LockNameCoreConverter lockNameCoreConverter;

    public DistributedLockAspect(LockNameCoreConverter lockNameCoreConverter) {
        this.lockNameCoreConverter = lockNameCoreConverter;
    }

    @Autowired(required = false)
    private RedissonClient redisson;

    @Around("@annotation(com.aaaxing.distributed.lock.annotation.DistributedLock)")
    public Object lockAspect(ProceedingJoinPoint joinPoint) throws Throwable {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Entered the aspect of distributedLock.");
        }

        DistributedLock annotation = getAndCheckLockAnnotation(joinPoint);

        if (redisson == null) {
            throw new DistributedLockException(1, "Required Redisson dependency for distributed lock was not found.");
        }

        String lockKey = lockNameCoreConverter.convertLockName(annotation.name(), joinPoint);

        lock(lockKey, annotation);
        Object proceed;
        try {
            proceed = joinPoint.proceed();
        } finally {
            unlock(lockKey, annotation);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Exited the aspect of distributedLock.");
        }
        return proceed;  
    }

    private DistributedLock getAndCheckLockAnnotation(ProceedingJoinPoint joinPoint) {

        DistributedLock annotation;

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            annotation = AnnotationUtils.getAnnotation(method, DistributedLock.class);
            Objects.requireNonNull(annotation);

        } catch (Exception e) {
            throw new DistributedLockException(0, "An exception occurred while get distributed lock annotation.",
                    e.getCause());
        }

        if (annotation.leaseTime() < -1) {
            throw new DistributedLockException(0, "The leaseTime value of the distributed lock annotation is invalid.");
        }
        if (annotation.waitTime() < 0) {
            throw new DistributedLockException(0, "The waitTime value of the distributed lock annotation is invalid.");
        }

        return annotation;
    }

    /**
     * 加锁
     *
     * @param lockKey 锁key
     * @param annotation 锁注解
     */
    private void lock(String lockKey, DistributedLock annotation) {
        Type type = annotation.type();
        Mode mode = annotation.mode();
        long leaseTime = annotation.leaseTime();
        long waitTime = annotation.waitTime();
        TimeUnit timeUnit = annotation.timeUnit();
        String tryLockFailMsg = annotation.tryLockFailMsg();

        try {

            if (Type.LOCK.equals(type)) {

                RLock lock = redisson.getLock(lockKey);

                if (Mode.LOCK.equals(mode)) {
                    lock.lock(leaseTime, timeUnit);
                } else if (Mode.TRY_LOCK.equals(mode)
                        && !lock.tryLock(waitTime, leaseTime, timeUnit)) {
                    throw new DistributedLockException(5, tryLockFailMsg);
                }

            } else if (Type.FAIR_LOCK.equals(type)) {

                RLock fairLock = redisson.getFairLock(lockKey);

                if (Mode.LOCK.equals(mode)) {
                    fairLock.lock(leaseTime, timeUnit);
                } else if (Mode.TRY_LOCK.equals(mode)
                        && !fairLock.tryLock(waitTime, leaseTime, timeUnit)) {
                    throw new DistributedLockException(5, tryLockFailMsg);
                }

            } else if (Type.READ_LOCK.equals(type)) {

                RReadWriteLock lock = redisson.getReadWriteLock(lockKey);
                RLock readLock = lock.readLock();

                if (Mode.LOCK.equals(mode)) {
                    readLock.lock(leaseTime, timeUnit);
                } else if (Mode.TRY_LOCK.equals(mode)
                        && !readLock.tryLock(waitTime, leaseTime, timeUnit)) {
                    throw new DistributedLockException(5, tryLockFailMsg);
                }

            } else if (Type.WRITE_LOCK.equals(type)) {

                RReadWriteLock lock = redisson.getReadWriteLock(lockKey);
                RLock writeLock = lock.writeLock();

                if (Mode.LOCK.equals(mode)) {
                    writeLock.lock(leaseTime, timeUnit);
                } else if (Mode.TRY_LOCK.equals(mode)
                        && !writeLock.tryLock(waitTime, leaseTime, timeUnit)) {
                    throw new DistributedLockException(5, tryLockFailMsg);
                }
            }

        } catch (Exception e) {
            if (e instanceof DistributedLockException) {
                throw (DistributedLockException) e;
            }
            throw new DistributedLockException(3, "An exception occurred while lock.", e.getCause());
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Lock succeed, {}-{}-{}", type, mode, lockKey);
        }
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁key
     * @param annotation 锁注解
     */
    private void unlock(String lockKey, DistributedLock annotation) {
        Type type = annotation.type();
        Mode mode = annotation.mode();
        boolean unlock = annotation.autoUnlock();

        try {

            if (unlock) {
                if (Type.LOCK.equals(type)) {
                    RLock lock = redisson.getLock(lockKey);
                    lock.unlock();
                } else if (Type.FAIR_LOCK.equals(type)) {
                    RLock fairLock = redisson.getFairLock(lockKey);
                    fairLock.unlock();
                } else if (Type.READ_LOCK.equals(type)) {
                    RReadWriteLock lock = redisson.getReadWriteLock(lockKey);
                    lock.readLock().unlock();
                } else if (Type.WRITE_LOCK.equals(type)) {
                    RReadWriteLock lock = redisson.getReadWriteLock(lockKey);
                    lock.writeLock().unlock();
                }
            }

        } catch (Exception e) {
            throw new DistributedLockException(4, "An exception occurred while unlock.", e.getCause());
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Unlock succeed, {}-{}-{}", type, mode, lockKey);
        }
    }

}