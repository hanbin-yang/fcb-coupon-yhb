package com.fcb.coupon.app.business.couponreceive.processor;

import com.fcb.coupon.app.business.couponreceive.CouponReceiveContext;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.dto.MessageReceiverDto;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.mq.producer.MessageProducer;
import com.fcb.coupon.common.enums.CouponDiscountType;
import com.fcb.coupon.common.enums.CouponSourceTypeEnum;
import com.fcb.coupon.common.enums.NotifyEventEnum;
import com.fcb.coupon.common.enums.NotifyTypeEnum;
import com.fcb.coupon.common.util.AESPromotionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 媒体广告领券后置处理器
 * @author YangHanBin
 * @date 2021-08-16 11:50
 */
@Component
@Slf4j
public class MediaCouponReceivePostProcessor extends AbstractCouponReceivePostProcessor {
    @Resource
    private MessageProducer messageProducer;

    @Override
    public boolean supports(Integer source) {
        return CouponSourceTypeEnum.COUPON_SOURCE_MEDIA_ADVERT.ifSame(source);
    }

    @Override
    public void postProcessBeforeReceive(CouponReceiveContext context) {
        super.postProcessBeforeReceive(context);
    }

    @Override
    public void postProcessAfterReceive(CouponReceiveContext context) {
        super.postProcessAfterReceive(context);
        // 发送短信通知
        sendSms(context);
    }

    private void sendSms(CouponReceiveContext context) {
        Map<String, String> content = new HashMap<>();
        CouponThemeCache couponThemeCache = context.getCouponThemeCache();
        if (CouponDiscountType.DISCOUNT.getType().equals(couponThemeCache.getCouponDiscountType())) {
            //折扣券数值除100
            String discountValue = new BigDecimal(couponThemeCache.getDiscountValue()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString().replaceAll("0+?$", "").replaceAll("[.]$", "");
            content.put("var1", discountValue + "折");
        } else {
            //金额券
            String discountAmount = couponThemeCache.getDiscountAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString().replaceAll("0+?$", "").replaceAll("[.]$", "");
            content.put("var1", discountAmount + "元");
        }

        content.put("var2", couponThemeCache.getThemeTitle());
        CouponEntity couponEntity = context.getCouponEntity();
        content.put("var3", AESPromotionUtil.decrypt(couponEntity.getCouponCode()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        content.put("var4", sdf.format(couponEntity.getEndTime()));

        MessageReceiverDto receiverDto = new MessageReceiverDto();
        receiverDto.setPhone(context.getUserMobile());
        receiverDto.setUid(context.getUserId());
        messageProducer.send("TRIGGER_EVENTSYSTEM-TOPIC", NotifyEventEnum.C_COUPON_AD_RECEIVE.getCode(), Collections.singletonList(NotifyTypeEnum.SMS), Collections.singletonList(receiverDto), content);
    }
}
