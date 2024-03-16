package com.fcb.coupon.backend.service;

import com.fcb.coupon.backend.model.bo.CouponMessageBo;
import com.fcb.coupon.backend.model.dto.MessageReceiverDto;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;

import java.util.List;

/**
 * @author 唐陆军
 * @Description 短信业务
 * @createTime 2021年08月06日 16:36:00
 */
public interface MessageService {

    /*
     * @description 发券消息
     * @author 唐陆军

     * @param: couponEntities
     * @date 2021-8-6 16:42
     */
    void sendCouponMessage(CouponThemeEntity couponThemeEntity, Integer source, Integer userType, List<MessageReceiverDto> receivers);


}
