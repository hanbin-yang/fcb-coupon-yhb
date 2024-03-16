package com.fcb.coupon.app.service;

import com.fcb.coupon.app.model.dto.CouponGrowingDto;

import java.util.List;

/**
 * @author mashiqiong
 * @date 2021-8-16 10:24
 */
public interface CouponGrowingService {
    void growingCouponsVerification(List<CouponGrowingDto> dtoList);

    void growingCouponsIssue(List<CouponGrowingDto> dto);

    void growingCouponsIssue(CouponGrowingDto dto);
}
