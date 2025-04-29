package com.example.AWMI.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;


/**
 支持异步处理
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}
