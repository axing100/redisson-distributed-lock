package com.aaaxing.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson配置类
 */
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