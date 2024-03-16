package com.fcb.coupon.app.business.couponreceive.processor;

import com.fcb.coupon.app.business.couponreceive.CouponReceiveContext;
import com.fcb.coupon.common.enums.CouponSourceTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 营销活动页券后置处理器
 * @author YangHanBin
 * @date 2021-08-18 14:42
 */
@Component
public class MarketingPageCouponReceivePostProcessor extends AbstractCouponReceivePostProcessor {
    @Override
    public boolean supports(Integer source) {
        return CouponSourceTypeEnum.COUPON_SOURCE_MARKETING_ACTIVITY.ifSame(source);
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