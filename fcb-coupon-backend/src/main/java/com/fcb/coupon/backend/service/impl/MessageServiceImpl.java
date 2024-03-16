package com.fcb.coupon.backend.service.impl;

import com.fcb.coupon.backend.model.dto.MessageReceiverDto;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.mq.producer.MessageProducer;
import com.fcb.coupon.backend.service.CouponThemeService;
import com.fcb.coupon.backend.service.MessageService;
import com.fcb.coupon.common.enums.*;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月06日 16:42:00
 */
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageProducer messageProducer;
    private final CouponThemeService couponThemeService;

    @Override
    public void sendCouponMessage(CouponThemeEntity couponThemeEntity, Integer source, Integer userType, List<MessageReceiverDto> receivers) {
        if (CollectionUtils.isEmpty(receivers)) {
            return;
        }
        NotifyEventEnum event = null;
        if (UserTypeEnum.B.getUserType().equals(userType)) {
            event = getNotifyEventForBroker(couponThemeEntity, source);
        } else if (UserTypeEnum.C.getUserType().equals(userType)) {
            event = getNotifyEventForCUser(couponThemeEntity, source);
        }
        if (event == null) {
            return;
        }


        Map<String, String> param = new HashMap<>();
        if (event.equals(NotifyEventEnum.c_coupon_issue_send_auto_code_welfare_card) || event.equals(NotifyEventEnum.b_coupon_issue_send_auto_code_welfare_card)) {
            param.put("var1", couponThemeEntity.getThemeTitle());
        } else {
            param.put("var1", couponThemeService.getCouponAmount(couponThemeEntity));
        }

        /**
         * 当前通知为主动营销发券成功时触发通知事件时，无参数2 var2
         */
        if (event.equals(NotifyEventEnum.c_coupon_issue_send_third_code) || event.equals(NotifyEventEnum.b_coupon_issue_send_third_code)) {
            param.put("var2", couponThemeEntity.getThemeTitle());
        }
        //发券三种渠道都发送
        List<NotifyTypeEnum> notifyTypeEnums = Lists.newArrayList(NotifyTypeEnum.SMS, NotifyTypeEnum.PUSH, NotifyTypeEnum.IMAIL);
        messageProducer.send(event.getCode(), receivers, param, notifyTypeEnums);
    }


    private NotifyEventEnum getNotifyEventForBroker(CouponThemeEntity couponThemeEntity, Integer source) {
        if (!CouponSourceTypeEnum.COUPON_SOURCE_NAMED_USER.getSource().equals(source)) {
            return NotifyEventEnum.coupon_issue_active;
        }
        if (CouponTypeEnum.COUPON_TYPE_THIRD.getType().equals(couponThemeEntity.getCouponType())) {
            return NotifyEventEnum.b_coupon_issue_send_third_code;
        }
        Integer couponDiscountType = couponThemeEntity.getCouponDiscountType();
        switch (CouponDiscountType.of(couponDiscountType)) {
            case CASH:
                return NotifyEventEnum.b_coupon_issue_send_value;
            case DISCOUNT:
                return NotifyEventEnum.b_coupon_issue_send_discount;
            case WELFARE_CARD:
                return NotifyEventEnum.b_coupon_issue_send_auto_code_welfare_card;
            case RED_ENVELOP:
                return NotifyEventEnum.b_coupon_issue_send_hongbao;
            default:
                log.warn("B端不支持的模板 couponThemeId={}", couponThemeEntity.getId());
                return null;
        }
    }

    private NotifyEventEnum getNotifyEventForCUser(CouponThemeEntity couponThemeEntity, Integer source) {
        if (!CouponSourceTypeEnum.COUPON_SOURCE_NAMED_USER.getSource().equals(source)) {
            return NotifyEventEnum.coupon_issue_active;
        }
        if (CouponTypeEnum.COUPON_TYPE_THIRD.getType().equals(couponThemeEntity.getCouponType())) {
            return NotifyEventEnum.c_coupon_issue_send_third_code;
        }

        Integer couponDiscountType = couponThemeEntity.getCouponDiscountType();
        switch (CouponDiscountType.of(couponDiscountType)) {
            case CASH:
                return NotifyEventEnum.c_coupon_issue_send_value;
            case DISCOUNT:
                return NotifyEventEnum.c_coupon_issue_send_discount;
            case WELFARE_CARD:
                return NotifyEventEnum.c_coupon_issue_send_auto_code_welfare_card;
            case RED_ENVELOP:
                return NotifyEventEnum.c_coupon_issue_send_hongbao;
            default:
                log.warn("B端不支持的模板 couponThemeId={}", couponThemeEntity.getId());
                return null;
        }
    }
}
