package com.fcb.coupon.backend.business.couponSend.processor.after;

import com.fcb.coupon.backend.business.couponSend.AfterSendProcessor;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.dto.MessageReceiverDto;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 唐陆军
 * @Description 发送短信处理
 * @createTime 2021年08月10日 17:05:00
 */
@Component
public class SendSmsAfterSendProcessor implements AfterSendProcessor {

    @Autowired
    private MessageService messageService;


    @Override
    public void process(List<CouponSendContext> sendCouponContexts, CouponThemeEntity couponTheme) {
        //发送短信、发送站内消息
        List<MessageReceiverDto> receivers = new ArrayList<>();
        for (CouponSendContext couponSendContext : sendCouponContexts) {
            if (couponSendContext.getIsFailure()) {
                continue;
            }
            MessageReceiverDto receiver = new MessageReceiverDto();
            receiver.setPhone(couponSendContext.getBindTel());
            receiver.setUid(couponSendContext.getUserId());
            receivers.add(receiver);
        }

        if (CollectionUtils.isEmpty(receivers)) {
            return;
        }

        Integer source = sendCouponContexts.get(0).getSource();
        Integer userType = sendCouponContexts.get(0).getUserType();
        messageService.sendCouponMessage(couponTheme, source, userType, receivers);
    }
}
