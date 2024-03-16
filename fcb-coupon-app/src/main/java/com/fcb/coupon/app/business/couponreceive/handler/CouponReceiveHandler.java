package com.fcb.coupon.app.business.couponreceive.handler;

import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.bo.CouponReceiveBo;
import com.fcb.coupon.app.model.entity.CouponEntity;

/**
 * @author YangHanBin
 * @date 2021-08-16 9:33
 */
public interface CouponReceiveHandler {
    boolean supports(Integer userType);

    void validate(CouponReceiveBo bo, CouponThemeCache couponTheme);

    CouponEntity handle(CouponReceiveBo bo, CouponThemeCache couponTheme);
}
