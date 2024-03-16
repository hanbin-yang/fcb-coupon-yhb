package com.fcb.coupon.backend.business.couponSend.processor.before;

import com.fcb.coupon.backend.business.couponSend.BeforeSendProcessor;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.CouponSendLogEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.entity.CouponUserStatisticEntity;
import com.fcb.coupon.backend.model.param.request.CouponSendUserRequest;
import com.fcb.coupon.backend.service.CouponSendLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 唐陆军
 * @Description 重复限制前置处理
 * @createTime 2021年08月24日 16:00:00
 */
@Component
public class DuplicateLimitBeforeSendProcessor implements BeforeSendProcessor {

    @Autowired
    private CouponSendLogService couponSendLogService;

    @Override
    public void process(List<CouponSendContext> sendCouponContexts, CouponThemeEntity couponTheme) {
        Set<String> transactionIdSet = sendCouponContexts.stream().map(m -> m.getTransactionId()).collect(Collectors.toSet());
        List<CouponSendLogEntity> sendLogEntities = couponSendLogService.listByThemeIdAndTransIds(couponTheme.getId(), new ArrayList<>(transactionIdSet));
        if (CollectionUtils.isEmpty(sendLogEntities)) {
            return;
        }

        Set<String> transactionIds = sendLogEntities.stream().map(m -> m.getTransactionId()).collect(Collectors.toSet());
        //判断是否存在
        for (CouponSendContext sendContext : sendCouponContexts) {
            if (transactionIds.contains(sendContext.getTransactionId())) {
                sendContext.error(false, "存在发券记录，不能重复发券");
                continue;
            }
        }
    }

}
