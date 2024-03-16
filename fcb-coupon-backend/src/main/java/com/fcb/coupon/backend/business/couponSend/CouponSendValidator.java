package com.fcb.coupon.backend.business.couponSend;

import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;

import java.util.List;

/*
优惠券限制策略
 */
public interface CouponSendValidator {


    void validate(List<CouponSendContext> couponSendContexts, CouponThemeCache couponTheme);

}
