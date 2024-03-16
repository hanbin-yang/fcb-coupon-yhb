package com.fcb.coupon.backend.business.couponSend.processor;

import com.fcb.coupon.backend.business.couponSend.AfterSendProcessor;
import com.fcb.coupon.backend.business.couponSend.BeforeSendProcessor;
import com.fcb.coupon.backend.business.couponSend.CouponSendPostProcessor;
import com.fcb.coupon.backend.business.couponSend.processor.after.SendSmsAfterSendProcessor;
import com.fcb.coupon.backend.business.couponSend.processor.after.SyncEsAfterSendProcessor;
import com.fcb.coupon.backend.business.couponSend.processor.before.DayLimitBeforeSendProcessor;
import com.fcb.coupon.backend.business.couponSend.processor.before.DuplicateLimitBeforeSendProcessor;
import com.fcb.coupon.backend.business.couponSend.processor.before.TotalLimitBeforeSendProcessor;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.service.CouponUserStatisticService;
import com.fcb.coupon.common.enums.CouponSourceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 唐陆军
 * @Description 主动营销发券前后置处理
 * @createTime 2021年08月18日 17:42:00
 */
@Slf4j
@Component
public class MarketingSendPostProcessor implements CouponSendPostProcessor {

    private List<BeforeSendProcessor> beforeSendProcessors = new ArrayList<>();
    private List<AfterSendProcessor> afterSendProcessors = new ArrayList<>();

    @Autowired
    private TotalLimitBeforeSendProcessor totalLimitBeforeSendProcessor;
    @Autowired
    private DuplicateLimitBeforeSendProcessor duplicateLimitBeforeSendProcessor;

    @Autowired
    private SendSmsAfterSendProcessor sendSmsAfterSendProcessor;
    @Autowired
    private SyncEsAfterSendProcessor syncEsAfterSendProcessor;

    @PostConstruct
    public void init() {
        beforeSendProcessors.add(totalLimitBeforeSendProcessor);
        beforeSendProcessors.add(duplicateLimitBeforeSendProcessor);

        afterSendProcessors.add(syncEsAfterSendProcessor);
        afterSendProcessors.add(sendSmsAfterSendProcessor);
    }


    @Override
    public Boolean supports(Integer source) {
        return CouponSourceTypeEnum.COUPON_SOURCE_MARKTEING_VOUCHER.getSource().equals(source);
    }

    @Override
    public void postProcessBeforeSend(List<CouponSendContext> sendCouponContexts, CouponThemeEntity couponTheme) {
        for (BeforeSendProcessor processor : beforeSendProcessors) {
            processor.process(sendCouponContexts, couponTheme);
        }
    }

    @Override
    public void postProcessAfterSend(List<CouponSendContext> sendCouponContexts, CouponThemeEntity couponTheme) {
        for (AfterSendProcessor processor : afterSendProcessors) {
            try {
                processor.process(sendCouponContexts, couponTheme);
            } catch (Exception ex) {
                log.error("发券后置处理异常", ex);
            }
        }
    }
}