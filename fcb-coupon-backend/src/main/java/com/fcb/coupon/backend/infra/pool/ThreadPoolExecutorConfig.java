package com.fcb.coupon.backend.infra.pool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author YangHanBin
 * @date 2021-06-16 19:29
 */
@Slf4j
@Configuration
@RefreshScope
public class ThreadPoolExecutorConfig {
    @Value("${thread.pool.coupon.common.corePoolSize:5}")
    private int commonCorePoolSize;
    @Value("${thread.pool.coupon.common.maxPoolSize:20}")
    private int commonMaxPoolSize;
    @Value("${thread.pool.coupon.common.capacity:10}")
    private int commonCapacity;

    @Value("${thread.pool.coupon.batch.corePoolSize:10}")
    private int batchCorePoolSize;
    @Value("${thread.pool.coupon.batch.maxPoolSize:20}")
    private int batchMaxPoolSize;
    @Value("${thread.pool.coupon.batch.capacity:10}")
    private int batchCapacity;

    /**
     * 优惠券通用线程池
     *
     * @return
     */
    @Bean(name = "couponCommonExecutor")
    public ThreadPoolTaskExecutor commonExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(commonCorePoolSize);
        executor.setMaxPoolSize(commonMaxPoolSize);
        executor.setQueueCapacity(commonCapacity);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("couponCommonExecutor-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }

    @Bean(name = "couponBatchExecutor")
    public ThreadPoolTaskExecutor couponBatchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(batchCorePoolSize);
        executor.setMaxPoolSize(batchMaxPoolSize);
        executor.setQueueCapacity(batchCapacity);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("couponBatchExecutor-");
        executor.setRejectedExecutionHandler(new CustomAbortPolicy());
        return executor;
    }

    @Bean(name = "couponVerificationExecutor")
    public ThreadPoolTaskExecutor couponVerificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(commonCorePoolSize);
        executor.setMaxPoolSize(commonCorePoolSize);
        executor.setQueueCapacity(commonCapacity);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("couponVerificationExecutor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }


}
