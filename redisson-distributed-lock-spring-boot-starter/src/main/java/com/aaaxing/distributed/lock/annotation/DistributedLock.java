// Copyright 2024 axing
package com.aaaxing.distributed.lock.annotation;

import com.aaaxing.distributed.lock.exception.DistributedLockException;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;
/**
 * 分布式锁注解
 *
 * @author axing
 * @date 2024-04-08
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * 锁名称
     * <p>
     *     1.可用{filed}占位符替换参数值，如{id}将会被替换为方法参数列表中名为id的参数值。                              <br/>
     *     2.使用“.”标记多层级，如{user.address.id}将会被替换为方法参数列表中名为user对象的address字段对象的id字段值。   <br/>
     *     3.若为2层级，且参数列表只有一个对象时，可以省略第1层级，如{user.id}可以简写为{id}。                           <br/>
     *     4.在{}中使用@userId替换当前登录用户userId（需要自定义过滤器注入）。                                             <br/>
     *     5.自动添加“lock:”前缀。                                                                                  <br/>
     * </p>
     */
    @AliasFor("value")
    String name() default "";

    /** 锁名称 */
    @AliasFor("name")
    String value() default "";

    /** 锁类型 */
    Type type() default Type.LOCK;

    /** 锁模式 */
    Mode mode() default Mode.LOCK;

    /**
     * 锁持有时间，超过这个时间或者方法结束都会释放锁。-1为自动续期
     * @see #timeUnit()
     */
    long leaseTime() default -1;

    /** 是否自动解锁，true会在方法结束后自动解锁；false仅在锁过期才会解锁 */
    boolean autoUnlock() default true;

    /**
     * 获取锁失败的提示信息，会以异常(status=5)的方式抛出，仅锁模式为TRY_LOCK有效
     * @see DistributedLockException
     */
    String tryLockFailMsg() default "try lock failed";

    /**
     * 锁等待时间，仅锁模式为TRY_LOCK有效，会在获取锁失败时在此时间内继续尝试获取锁
     * @see #timeUnit()
     */
    long waitTime() default 0;

    /**
     * 时间单位，对ttl、waitTime都生效
     * @see #leaseTime()
     * @see #waitTime()
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;



    enum Type {
        /**
         * 可重入锁
         * @see RedissonClient#getLock(String)
         */
        LOCK,

        /**
         * 公平锁
         * @see RedissonClient#getFairLock(String)
         */
        FAIR_LOCK,

        /**
         * 读锁（与写锁配合使用）
         * @see RedissonClient#getReadWriteLock(String)
         * @see RReadWriteLock#readLock()
         */
        READ_LOCK,

        /**
         * 写锁（与读锁配合使用）
         * @see RedissonClient#getReadWriteLock(String)
         * @see RReadWriteLock#writeLock()
         */
        WRITE_LOCK,
    }

    enum Mode {
        /**
         * 阻断等待获取锁
         * @see RLock#lock(long, TimeUnit)
         */
        LOCK,
        /**
         * 尝试加锁失败立即抛出status=5的异常；若配置了等待时间则会在指定时间内继续尝试获取锁，超时后再抛出status=5的异常
         * @see RLock#tryLock(long, long, TimeUnit)
         * @see DistributedLockException
         */
        TRY_LOCK,
    }
}
