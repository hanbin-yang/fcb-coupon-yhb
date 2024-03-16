package com.fcb.coupon.backend.service;

import com.fcb.coupon.backend.model.dto.CouponGrowingDto;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

/**
 * @author mashiqiong
 * @date 2021-8-16 10:24
 */
public interface CouponGrowingService {
    @Async("couponGrowingExecutor")
    void growingCouponsVerification(List<CouponGrowingDto> dtoList);

    void growingCouponsIssue(List<CouponGrowingDto> dto);

    @Async("couponGrowingExecutor")
    void growingCouponsIssue(CouponGrowingDto dto);
}
