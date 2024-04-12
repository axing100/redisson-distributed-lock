# redisson-distributed-lock

#### 介绍
1. 分布式锁基于redisson、spring-aop实现，仅支持spring-boot应用

2. 通过给方法添加@DistributedLock注解来非常方便的使用分布式锁，锁名支持占位符替换任意层级方法入参字段值，支持可重入锁、公平锁、读写锁。

3. 通过DistributedLocks工具类使用分布式锁，同样支持可重入锁、公平锁、读写锁。

4. redisson本身提供了丰富的锁相关API与看门狗自动续期机制，本项目进行了封装与扩展，没有改变redisson本身的机制

<br/>

#### 工程模块

|                      模块                       |      说明       |
|:---------------------------------------------:|:-------------:|
|       redisson-distributed-lock-parent        |      父工程      |
| redisson-distributed-lock-spring-boot-starter | 分布式锁的starter	 |
|       redisson-distributed-lock-example       |     示例程序      |

<br/>

#### 快速开始

1. 在你的项目maven中引入依赖

```xml
<dependency>
    <groupId>com.aaaxing</groupId>
    <artifactId>redisson-distributed-lock-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. 添加redisson配置类（也可以忽略这步从而使用默认配置）
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

3. 为你的service或controller方法添加@DistributedLock注解使用分布式锁（推荐，相当优雅！）
```java
// 基础使用
@DistributedLock("test")
public void test1() {

}
// 通过占位符缩小锁粒度
@DistributedLock("test:{id}:{type}")
public void test2(Long id, String type) {

}
// 占位符支持多层级
@DistributedLock("test:{order.productId}")
public void test3(Order order) {

}
```
4. 也可以使用DistributedLocks工具类使用分布式锁
```java
// lock用法
public void test() {
    String lockName = "test";
    DistributedLocks.lock(lockName);
    try {
        // manipulate protected state
    } finally {
        DistributedLocks.unlock(lockName);
    }
}

// tryLock用法
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
5. 启动redis
6. 启动你的项目

<br/>

#### 注解参数

##### @DistributedLock

##### name：锁名称
1. 可用{filed}占位符替换参数值，如{id}将会被替换为方法参数列表中名为id的参数值。

2. 使用“.”标记多层级，如{user.address.id}将会被替换为方法参数列表中名为user对象的address字段对象的id字段值。

3. 若为2层级，且参数列表只有一个对象时，可以省略第1层级，如{user.id}可以简写为{id}。

4. 使用{@userId}替换当前登录用户userId（过滤器自行注入），可实现LockNamePreConverter接口注册bean应用自定义逻辑。

5. 默认添加“lock:”前缀，可通过application.properties配置：distributed-lock.prefix=lock:

##### 全部参数：
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    /** 锁名称 */
    @AliasFor("value")
    String name() default "";

    /** 锁名称别名 */
    @AliasFor("name")
    String value() default "";

    /** 锁类型 */
    Type type() default Type.LOCK;

    /** 锁模式 */
    Mode mode() default Mode.LOCK;

    /** 锁持有时间，超过这个时间或者方法结束都会释放锁。-1为自动续期 */
    long leaseTime() default -1;

    /** 是否自动解锁，true会在方法结束后自动解锁；false仅在锁过期才会解锁 */
    boolean autoUnlock() default true;

    /** 获取锁失败的提示信息，会以异常(status=5)的方式抛出，仅锁模式为TRY_LOCK有效 */
    String tryLockFailMsg() default "try lock failed";

    /** 锁等待时间，仅锁模式为TRY_LOCK有效，会在获取锁失败时在此时间内继续尝试获取锁 */
    long waitTime() default 0;

    /** 时间单位，对ttl、waitTime都生效 */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    
    enum Type {
        /** 可重入锁 */
        LOCK,
        /** 公平锁 */
        FAIR_LOCK,
        /** 读锁（与写锁配合使用） */
        READ_LOCK,
        /** 写锁（与读锁配合使用） */
        WRITE_LOCK,
    }

    enum Mode {
        /** 阻断等待获取锁 */
        LOCK,
        /** 尝试加锁失败立即抛出status=5的异常；若配置了等待时间则会在指定时间内继续尝试获取锁，超时后再抛出status=5的异常 */
        TRY_LOCK,
    }
}
```

<br/>

#### 更多功能
更多功能与用法请参看示例程序或源码注释

<br/>

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request
