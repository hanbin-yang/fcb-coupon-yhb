package com.fcb.coupon.backend.business.couponSend;

import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;

import java.util.List;

public interface CouponSendStrategy {

    Boolean supports(Integer couponType);

    void batchSend(List<CouponSendContext> couponSendContexts, CouponThemeEntity couponTheme);

}
