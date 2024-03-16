package com.fcb.coupon.backend.business.couponSend;

import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月06日 09:07:00
 */
public interface CouponSendPostProcessor {

    /*
     * @description 是否支持
     * @author 唐陆军
     * @param: source
     * @date 2021-8-6 9:37
     * @return: java.lang.Boolean
     */
    Boolean supports(Integer source);

    /*
     * @description 发券前处理 主要是校验
     * @author 唐陆军
     */
    default void postProcessBeforeSend(List<CouponSendContext> sendCouponContexts, CouponThemeEntity couponTheme) {
    }

    /*
     * @description 发券后处理  同步es 需判断成功 发送短信 记录错误日志 发送站内消息 记录用户领券统计表
     * @author 唐陆军
     */
    default void postProcessAfterSend(List<CouponSendContext> sendCouponContexts, CouponThemeEntity couponTheme) {

    }
}
