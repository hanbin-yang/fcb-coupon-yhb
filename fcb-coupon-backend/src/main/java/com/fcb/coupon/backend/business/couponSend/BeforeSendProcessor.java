package com.fcb.coupon.backend.business.couponSend;

import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;

import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月24日 15:44:00
 */
public interface BeforeSendProcessor {

    void process(List<CouponSendContext> sendCouponContexts, CouponThemeEntity couponTheme);
}
