package com.fcb.coupon.backend.business.couponSend;

import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;

import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月11日 10:34:00
 */
public interface AfterSendProcessor {

    void process(List<CouponSendContext> sendCouponContexts, CouponThemeEntity couponTheme);
}
