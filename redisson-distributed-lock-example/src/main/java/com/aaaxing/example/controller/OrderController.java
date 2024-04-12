package com.aaaxing.example.controller;

import com.aaaxing.distributed.lock.annotation.DistributedLock;
import com.aaaxing.distributed.lock.utils.DistributedLocks;
import com.aaaxing.example.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.aaaxing.distributed.lock.annotation.DistributedLock.*;

/**
 * @author axing
 * @date 2024-04-11
 */
@Slf4j
@RestController
public class OrderController {

    /**
     * 注解方式使用分布式锁，推荐
     */
    @PostMapping("/order/save")
    @DistributedLock("saveOrder:{order.productId}")
//    @DistributedLock("saveOrder:{productId}")
//    @DistributedLock(name = "saveOrder:{productId}", type = Type.LOCK)
//    @DistributedLock(name = "saveOrder:{productId}", type = Type.FAIR_LOCK)
//    @DistributedLock(name = "saveOrder:{productId}", type = Type.READ_LOCK)
//    @DistributedLock(name = "saveOrder:{productId}", type = Type.WRITE_LOCK)
//    @DistributedLock(name = "saveOrder:{productId}", type = Type.LOCK, mode = Mode.TRY_LOCK, leaseTime = 60, waitTime = 2,
//            timeUnit = TimeUnit.SECONDS, autoUnlock = true, tryLockFailMsg = "访问用户过多，请您稍后再试~")
    public Map<String, Object> save(@RequestBody Order order) throws InterruptedException {
        log.info("The order logic begins. productId: {}, requestId: {}", order.getProductId(), order.getRequestId());



        DistributedLocks.lock("我的锁");
        Thread.sleep(5000);
        DistributedLocks.unlock("我的锁");

        log.info("The order logic is over. productId: {}, requestId: {}", order.getProductId(), order.getRequestId());
        HashMap<String, Object> result = new HashMap<>(4);
        result.put("status", 0);
        result.put("message", "success");
        return result;
    }

    // 下方测试读写锁
    // mock db
    private static final ConcurrentHashMap<Long, Order> ID_ORDER_MAP = new ConcurrentHashMap<>();
    static {
        Order order = new Order();
        order.setId(1L);
        order.setProductId(20001L);
        order.setNum(1);
        ID_ORDER_MAP.put(order.getId(), order);
    }

    @GetMapping("/order/{id}")
    @DistributedLock(name = "order", type = Type.READ_LOCK)
    public Map<String, Object> get(@PathVariable Long id) throws InterruptedException {

        // Thread.sleep(10000);

        HashMap<String, Object> result = new HashMap<>(4);
        result.put("status", 0);
        result.put("message", "success");
        result.put("data", ID_ORDER_MAP.get(id));

        return result;
    }

    @PutMapping("/order/{id}")
    @DistributedLock(name = "order", type = Type.WRITE_LOCK)
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Order order) throws InterruptedException {

        Thread.sleep(10000);

        Order orderEntity = ID_ORDER_MAP.get(id);
        orderEntity.setNum(order.getNum());

        HashMap<String, Object> result = new HashMap<>(4);
        result.put("status", 0);
        result.put("message", "success");
        return result;
    }

    /**
     * 测试自定义锁名预转换器逻辑
     */
    @GetMapping("/custom")
    @DistributedLock(name = "custom:{@ip}", mode = Mode.TRY_LOCK, leaseTime = 1,
            autoUnlock = false, tryLockFailMsg = "请求过于频繁~请稍后再试")
    public String custom() {
        return "success";
    }

    /**
     * 工具类方式使用分布式锁
     */
    @GetMapping("/test")
    public String test() throws InterruptedException {
        String lockName = "test";
        // lock
        DistributedLocks.lock(lockName);
        try {
            // manipulate protected state
            log.info("lock thread: {}", Thread.currentThread().getId());
            Thread.sleep(5000);
        } finally {
            log.info("unlock thread: {}", Thread.currentThread().getId());
            DistributedLocks.unlock(lockName);
        }

//        // tryLock
//        if (DistributedLocks.tryLock(lockName)) {
//            try {
//                // manipulate protected state
//                log.info("lock thread: {}", Thread.currentThread().getName());
//                Thread.sleep(5000);
//            } finally {
//                log.info("unlock thread: {}", Thread.currentThread().getId());
//                DistributedLocks.unlock(lockName);
//            }
//        } else {
//            // perform alternative actions
//            log.info("the lock is occupied");
//        }

        return "success";
    }
}
