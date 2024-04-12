# redisson-distributed-lock

#### Description
1. Distributed lock is implemented based on redisson and spring-aop, and only supports spring-boot applications.

2. It is very convenient to use distributed locks by adding the @DistributedLock annotation to the method. 
The lock name supports placeholders to replace the parameter field values in any level method, 
and supports reentrant locks, fair locks, and read-write locks.

3. Distributed locks are used through the DistributedLocks tool class, 
which also supports reentrant locks, fair locks, and read-write locks.

4. Redisson itself provides a wealth of lock-related APIs and automatic watchdog renewal mechanisms. 
This project has been encapsulated and expanded without changing the mechanism of Redisson itself.

<br/>

#### Software module

|                    Module                     |        description        |
|:---------------------------------------------:|:-------------------------:|
|       redisson-distributed-lock-parent        |       parent module       |
| redisson-distributed-lock-spring-boot-starter | distributed lock starter	 |
|       redisson-distributed-lock-example       |      sample program       |

<br/>

#### Quick start

1. Introduce dependencies into your maven project

```xml
<dependency>
    <groupId>com.aaaxing</groupId>
    <artifactId>redisson-distributed-lock-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

2. Add the redisson configuration class (you can also ignore this step and use the default configuration)
```java
@Configuration
public class RedissonConfig {
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword(null)
                .setDatabase(0)
                .setConnectionPoolSize(64)
                .setTimeout(3000);
        config.setCodec(new JsonJacksonCodec(new ObjectMapper()));
        config.setLockWatchdogTimeout(30 * 1000);
        return Redisson.create(config);
    }
}
```

3. Add @DistributedLock annotation to your service or controller method to use distributed lock 
(recommended, quite elegant!)
```java
// Basic usage
@DistributedLock("test")
public void test1() {

}
// Reduce lock granularity through placeholders
@DistributedLock("test:{id}:{type}")
public void test2(Long id, String type) {

}
// Placeholders support multiple levels
@DistributedLock("test:{order.productId}")
public void test3(Order order) {

}
```
4. You can also use the DistributedLocks tool class to use distributed locks
```java
// lock
public void test() {
    String lockName = "test";
    DistributedLocks.lock(lockName);
    try {
        // manipulate protected state
    } finally {
        DistributedLocks.unlock(lockName);
    }
}

// tryLock
if (DistributedLocks.tryLock(lockName)) {
    try {
        // manipulate protected state
        
    } finally {
        DistributedLocks.unlock(lockName);
    }
} else {
    // perform alternative actions
    
}
```
5. Start redis
6. Start your project

<br/>

#### Annotation parameter

##### @DistributedLock

##### name：lock name
1. You can replace parameter values with {filed} placeholders. For example, {id} will be replaced with the parameter 
value named id in the method parameter list.

2. Use "." to mark multiple levels. For example, {user.address.id} will be replaced by the id field value of the 
address field object named user object in the method parameter list.

3. If it is 2 levels and the parameter list has only one object, the first level can be omitted. 
For example, {user.id} can be abbreviated as {id}.

4. Use {@userId} to replace the userId of the currently logged-in user (the filter is injected by itself), 
and the LockNamePreConverter interface can be used to register bean application custom logic.

5. The "lock:" prefix is added by default, which can be configured through 
application.properties: distributed-lock.prefix=lock:

##### All parameters：
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    /** Lock name */
    @AliasFor("value")
    String name() default "";

    /** Alias of lock name */
    @AliasFor("name")
    String value() default "";

    /** Lock type */
    Type type() default Type.LOCK;

    /** Lock mode */
    Mode mode() default Mode.LOCK;

    /** The lock is held for a time, and the lock will be released after this time or when the method ends. 
     * -1 means automatic renewal */
    long leaseTime() default -1;

    /** Whether to automatically unlock, true will automatically unlock after the method ends; 
     * false will only unlock when the lock expires */
    boolean autoUnlock() default true;

    /** Prompt information for failure to obtain the lock will be thrown as an exception (status=5). 
     * It is only valid when the lock mode is TRY_LOCK. */
    String tryLockFailMsg() default "try lock failed";

    /** Lock waiting time, only valid when the lock mode is TRY_LOCK. If the lock fails to be acquired, 
     * it will continue to try to acquire the lock within this time. */
    long waitTime() default 0;

    /** Time unit, valid for both ttl and waitTime */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    
    enum Type {
        /** Reentrant lock */
        LOCK,
        /** Fair lock */
        FAIR_LOCK,
        /** Read lock (used with write lock) */
        READ_LOCK,
        /** Write lock (used in conjunction with read lock) */
        WRITE_LOCK,
    }

    enum Mode {
        /** Block waiting to acquire lock */
        LOCK,
        /** 
         * If the lock attempt fails, an exception with status=5 will be thrown immediately; 
         * if the waiting time is configured, it will continue to try to acquire the lock within the specified time, 
         * and then throw an exception with status=5 after timeout. */
        TRY_LOCK,
    }
}
```

<br/>

#### More features
For more functions and usage, please refer to the sample program or source code comments.

<br/>

#### Contribution

1.  Fork the repository
2.  Create Feat_xxx branch
3.  Commit your code
4.  Create Pull Request
