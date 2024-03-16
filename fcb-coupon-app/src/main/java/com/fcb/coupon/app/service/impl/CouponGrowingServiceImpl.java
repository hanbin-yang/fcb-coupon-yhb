package com.fcb.coupon.app.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.fcb.coupon.app.model.dto.CouponGrowingDto;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.entity.CouponUserEntity;
import com.fcb.coupon.app.model.query.LambdaFieldNameSelector;
import com.fcb.coupon.app.service.CouponGrowingService;
import com.fcb.coupon.app.service.CouponThemeCacheService;
import com.fcb.coupon.app.service.CouponUserService;
import com.fcb.coupon.common.enums.CouponGiveRuleEnum;
import io.growing.sdk.java.GrowingAPI;
import io.growing.sdk.java.dto.GioCdpEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author mashiqiong
 * @date 2021-8-16 10:25
 */
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class CouponGrowingServiceImpl implements CouponGrowingService {

    private final CouponThemeCacheService couponThemeCacheService;
    private  final CouponUserService couponUserService;
    private final GrowingAPI growingAPI;


    @Override
    public void growingCouponsVerification(List<CouponGrowingDto> dtoList) {
        for (CouponGrowingDto dto : dtoList) {
            LambdaFieldNameSelector<CouponThemeCache> selector = new LambdaFieldNameSelector<>(CouponThemeCache.class);
            selector.select(CouponThemeCache::getThemeTitle);
            CouponThemeCache couponThemeCache = couponThemeCacheService.getById(dto.getCouponThemeId(), selector);
            CouponUserEntity dbCouponUser = couponUserService.getById(dto.getCouponId());
            if (Objects.equals(CouponGiveRuleEnum.COUPON_GIVE_RULE_ACTIVY_RULE.getType(), couponThemeCache.getCouponGiveRule())) {
                try {
                    GioCdpEventMessage msg = new GioCdpEventMessage.Builder()
                            .eventTime(System.currentTimeMillis())// 事件时间，默认为系统时间（选填）
                            .eventKey("couponsVerification")// 事件标识 (必填)
                            .loginUserId(dto.getUnionId())// 登录用户ID (必填)
                            .addEventVariable("couponsID_var", couponThemeCache.getId().toString())// 事件级变量 (选填)
                            .addEventVariable("couponsName_var", couponThemeCache.getThemeTitle()) // 事件级变量 (选填)
                            .addEventVariable("issueTime_var", DateUtil.format(dbCouponUser.getCreateTime(), DatePattern.NORM_DATETIME_PATTERN)) // 事件级变量 (选填)
                            .addEventVariable("verificationTime_var", DateUtil.format(dto.getUsedTime(), DatePattern.NORM_DATETIME_PATTERN)) // 事件级变量 (选填)
                            .addEventVariable("lpName_var", dto.getUsedStoreName()) // 事件级变量 (选填)
                            .addEventVariable("lpID_var", dto.getUsedStoreId().toString()) // 事件级变量 (选填)
                            .build();
                    growingAPI.send(msg);
                } catch (Exception e) {
                    log.error("核销券埋点异常："+e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void growingCouponsIssue(CouponGrowingDto dto) {
        LambdaFieldNameSelector<CouponThemeCache> selector = new LambdaFieldNameSelector<>(CouponThemeCache.class);
        selector.select(CouponThemeCache::getThemeTitle);
        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(dto.getCouponThemeId(), selector);

        if(Objects.equals(CouponGiveRuleEnum.COUPON_GIVE_RULE_ACTIVY_RULE.getType(), couponThemeCache.getCouponGiveRule())) {
            try {
                GioCdpEventMessage msg = new GioCdpEventMessage.Builder()
                        .eventTime(System.currentTimeMillis())// 事件时间，默认为系统时间（选填）
                        .eventKey("couponsIssue")// 事件标识 (必填)
                        .loginUserId(dto.getUnionId())// 登录用户ID (必填)
                        .addEventVariable("couponsID_var", couponThemeCache.getId().toString())// 事件级变量 (选填)
                        .addEventVariable("couponsName_var", couponThemeCache.getThemeTitle()) // 事件级变量 (选填)
                        .addEventVariable("issueTime_var", DateUtil.format(dto.getBindTime(), DatePattern.NORM_DATETIME_PATTERN)) // 事件级变量 (选填)
                        .build();
                growingAPI.send(msg);
            } catch (Exception e) {
                log.error("发券埋点异常："+e.getMessage(), e);
            }
        }
    }

    @Override
    public void growingCouponsIssue(List<CouponGrowingDto> dtoList) {
        for (CouponGrowingDto dto : dtoList) {
            growingCouponsIssue(dto);
        }
    }
}
