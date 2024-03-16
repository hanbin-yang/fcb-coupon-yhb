package com.fcb.coupon.app.business.couponreceive.processor;

import com.fcb.coupon.app.business.couponreceive.CouponReceiveContext;
import com.fcb.coupon.common.enums.CouponSourceTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 前台领券后置处理器
 * @author YangHanBin
 * @date 2021-08-17 11:27
 */
@Component
public class InitiativeCouponReceivePostProcessor extends AbstractCouponReceivePostProcessor {
    @Override
    public boolean supports(Integer source) {
        return CouponSourceTypeEnum.COUPON_SOURCE_ACTIVITY.ifSame(source);
    }

    @Override
    public void postProcessBeforeReceive(CouponReceiveContext context) {
        super.postProcessBeforeReceive(context);
    }

    @Override
    public void postProcessAfterReceive(CouponReceiveContext context) {
        super.postProcessAfterReceive(context);
    }
}
