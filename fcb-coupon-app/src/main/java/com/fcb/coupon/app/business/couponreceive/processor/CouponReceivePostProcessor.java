package com.fcb.coupon.app.business.couponreceive.processor;

import com.fcb.coupon.app.business.couponreceive.CouponReceiveContext;

/**
 * @author YangHanBin
 * @date 2021-08-16 11:36
 */
public interface CouponReceivePostProcessor {
    boolean supports(Integer source);

    default void postProcessBeforeReceive(CouponReceiveContext context) {

    }

    default void postProcessAfterReceive(CouponReceiveContext context) {
    }
}
