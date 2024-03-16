package com.fcb.coupon.app.business.couponreceive.strategy;

import com.fcb.coupon.app.business.couponreceive.CouponReceiveContext;
import com.fcb.coupon.app.model.entity.CouponEntity;

/**
 * 发券策略
 * @author YangHanBin
 * @date 2021-08-16 13:54
 */
public interface CouponReceiveStrategy {
    boolean supports(Integer couponType);

    CouponEntity receive(CouponReceiveContext context);
}
