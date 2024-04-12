// Copyright 2024 axing
package com.aaaxing.distributed.lock.utils;

import com.aaaxing.distributed.lock.annotation.DistributedLock;
import com.aaaxing.distributed.lock.exception.DistributedLockException;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁操作类，封装了redisson的常用锁API
 *
 * <br>注解方式可以满足使用场景的话，更推荐使用注解@DistributedLock
 * @see DistributedLock
 * @author axing
 * @date 2024-04-11
 */
public class DistributedLocks {
    private static RedissonClient redisson;
    private static String prefix = "";

    public static void setRedisson(RedissonClient redisson) {
        DistributedLocks.redisson = redisson;
    }

    public static void setPrefix(String prefix) {
        DistributedLocks.prefix = prefix;
    }



    public static void lock(String name) {
        redisson.getLock(prefix + name).lock();
    }

    public static void lock(String name, long leaseTime) {
        redisson.getLock(prefix + name).lock(leaseTime, TimeUnit.SECONDS);
    }

    public static void lock(String name, long leaseTime, TimeUnit unit) {
        redisson.getLock(prefix + name).lock(leaseTime, unit);
    }

    public static boolean tryLock(String name) {
        return redisson.getLock(prefix + name).tryLock();
    }

    public static boolean tryLock(String name, long waitTime) {
        return tryLock(name, waitTime, -1, TimeUnit.SECONDS);
    }

    public static boolean tryLock(String name, long waitTime, TimeUnit unit) {
        return tryLock(name, waitTime, -1, unit);
    }

    public static boolean tryLock(String name, long waitTime, long leaseTime) {
        return tryLock(name, waitTime, leaseTime, TimeUnit.SECONDS);
    }

    public static boolean tryLock(String name, long waitTime, long leaseTime, TimeUnit unit) {
        try {
            return redisson.getLock(prefix + name).tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            throw new DistributedLockException(3, e.getMessage(), e.getCause());
        }
    }

    public static void unlock(String name) {
        redisson.getLock(prefix + name).unlock();
    }



    public static void lockFairLock(String name) {
        redisson.getFairLock(prefix + name).lock();
    }

    public static void lockFairLock(String name, long leaseTime) {
        redisson.getFairLock(prefix + name).lock(leaseTime, TimeUnit.SECONDS);
    }

    public static void lockFairLock(String name, long leaseTime, TimeUnit unit) {
        redisson.getFairLock(prefix + name).lock(leaseTime, unit);
    }

    public static boolean tryLockFairLock(String name) {
        return redisson.getFairLock(prefix + name).tryLock();
    }

    public static boolean tryLockFairLock(String name, long waitTime) {
        return tryLockFairLock(name, waitTime, -1, TimeUnit.SECONDS);
    }

    public static boolean tryLockFairLock(String name, long waitTime, TimeUnit unit) {
        return tryLockFairLock(name, waitTime, -1, unit);
    }

    public static boolean tryLockFairLock(String name, long waitTime, long leaseTime) {
        return tryLockFairLock(name, waitTime, leaseTime, TimeUnit.SECONDS);
    }

    public static boolean tryLockFairLock(String name, long waitTime, long leaseTime, TimeUnit unit) {
        try {
            return redisson.getFairLock(prefix + name).tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            throw new DistributedLockException(3, e.getMessage(), e.getCause());
        }
    }

    public static void unlockFairLock(String name) {
        redisson.getFairLock(prefix + name).unlock();
    }



    public static void lockReadLock(String name) {
        redisson.getReadWriteLock(prefix + name).readLock().lock();
    }

    public static void lockReadLock(String name, long leaseTime) {
        lockReadLock(name, leaseTime, TimeUnit.SECONDS);
    }

    public static void lockReadLock(String name, long leaseTime, TimeUnit unit) {
        redisson.getReadWriteLock(prefix + name).readLock().lock(leaseTime, unit);
    }

    public static boolean tryLockReadLock(String name) {
        return redisson.getReadWriteLock(prefix + name).readLock().tryLock();
    }

    public static boolean tryLockReadLock(String name, long waitTime) {
        return tryLockReadLock(name, waitTime, -1, TimeUnit.SECONDS);
    }

    public static boolean tryLockReadLock(String name, long waitTime, TimeUnit unit) {
        return tryLockReadLock(name, waitTime, -1, unit);
    }

    public static boolean tryLockReadLock(String name, long waitTime, long leaseTime) {
        return tryLockReadLock(name, waitTime, leaseTime, TimeUnit.SECONDS);
    }

    public static boolean tryLockReadLock(String name, long waitTime, long leaseTime, TimeUnit unit) {
        try {
            return redisson.getReadWriteLock(prefix + name).readLock().tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            throw new DistributedLockException(3, e.getMessage(), e.getCause());
        }
    }

    public static void unLockReadLock(String name) {
        redisson.getReadWriteLock(prefix + name).readLock().unlock();
    }



    public static void lockWriteLock(String name) {
        redisson.getReadWriteLock(prefix + name).writeLock().lock();
    }

    public static void lockWriteLock(String name, long leaseTime) {
        lockWriteLock(name, leaseTime, TimeUnit.SECONDS);
    }

    public static void lockWriteLock(String name, long leaseTime, TimeUnit unit) {
        redisson.getReadWriteLock(prefix + name).writeLock().lock(leaseTime, unit);
    }

    public static boolean tryLockWriteLock(String name) {
        return redisson.getReadWriteLock(prefix + name).writeLock().tryLock();
    }

    public static boolean tryLockWriteLock(String name, long waitTime) {
        return tryLockWriteLock(name, waitTime, -1, TimeUnit.SECONDS);
    }

    public static boolean tryLockWriteLock(String name, long waitTime, TimeUnit unit) {
        return tryLockWriteLock(name, waitTime, -1, unit);
    }

    public static boolean tryLockWriteLock(String name, long waitTime, long leaseTime) {
        return tryLockWriteLock(name, waitTime, leaseTime, TimeUnit.SECONDS);
    }

    public static boolean tryLockWriteLock(String name, long waitTime, long leaseTime, TimeUnit unit) {
        try {
            return redisson.getReadWriteLock(prefix + name).writeLock().tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            throw new DistributedLockException(3, e.getMessage(), e.getCause());
        }
    }

    public static void unlockWriteLock(String name) {
        redisson.getReadWriteLock(prefix + name).writeLock().unlock();
    }
}
