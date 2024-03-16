package com.fcb.coupon.backend.business.verification.context;

import com.fcb.coupon.backend.remote.client.BrokerClient;
import com.fcb.coupon.backend.remote.client.CommonFileClient;
import com.fcb.coupon.backend.remote.client.CustomerClient;
import com.fcb.coupon.backend.remote.client.OuserWebFeignClient;
import com.fcb.coupon.backend.service.*;
import com.fcb.coupon.common.excel.exporter.ExcelExporter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author YangHanBin
 * @date 2021-09-09 14:35
 */
@Component
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Getter
public class VerifyServiceContext {
    private final AsyncTaskService asyncTaskService;
    private final ExcelExporter excelExporter;
    private final CommonFileClient commonFileClient;
    private final CouponThemeCacheService couponThemeCacheService;
    private final CouponEsDocService couponEsDocService;
    private final CouponService couponService;
    private final CustomerClient customerClient;
    private final BrokerClient brokerClient;
    private final OuserWebFeignClient ouserWebFeignClient;
    private final MktUseRuleService mktUseRuleService;
    private final ThreadPoolTaskExecutor couponVerificationExecutor;
    private final KafkaTemplate kafkaTemplate;
    private final CouponOprLogService couponOprLogService;
    private final ApplicationEventPublisher publisher;
    private final CouponVerificationService couponVerificationService;
    // 适用楼盘cache
    private final Cache<Long, Set<Long>> applicableStoreCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build();
    private final Interner<Long> couponThemeIdIntern = Interners.newWeakInterner();
}
