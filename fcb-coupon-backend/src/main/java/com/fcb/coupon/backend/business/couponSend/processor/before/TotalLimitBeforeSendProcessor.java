package com.fcb.coupon.backend.business.couponSend.processor.before;

import com.fcb.coupon.backend.business.couponSend.BeforeSendProcessor;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.entity.CouponUserStatisticEntity;
import com.fcb.coupon.backend.service.CouponUserStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 唐陆军
 * @Description 总数限制前置处理
 * @createTime 2021年08月24日 15:49:00
 */
@Component
public class TotalLimitBeforeSendProcessor implements BeforeSendProcessor {

    @Autowired
    protected CouponUserStatisticService couponUserStatisticService;

    @Override
    public void process(List<CouponSendContext> sendCouponContexts, CouponThemeEntity couponTheme) {
        if (CollectionUtils.isEmpty(sendCouponContexts)) {
            return;
        }
        //判断总数无限制
        if (couponTheme.getIndividualLimit() == null || couponTheme.getIndividualLimit() <= 0) {
            return;
        }
        Integer userType = sendCouponContexts.get(0).getUserType();
        List<String> userIds = sendCouponContexts.stream().map(m -> m.getUserId()).collect(Collectors.toList());
        List<CouponUserStatisticEntity> couponUserStatisticEntities = couponUserStatisticService.listByUserIds(couponTheme.getId(), userType, userIds);
        Map<String, CouponUserStatisticEntity> couponUserStatisticMap = couponUserStatisticEntities.stream().collect(Collectors.toMap(m -> m.getUserId(), m -> m));
        //校验
        for (CouponSendContext sendContext : sendCouponContexts) {
            CouponUserStatisticEntity statisticEntity = couponUserStatisticMap.get(sendContext.getUserId());
            if (statisticEntity == null) {
                continue;
            }
            if (statisticEntity.getTotalCount() >= couponTheme.getIndividualLimit()) {
                sendContext.error(false, "超过每个ID总共可以领取限制");
                continue;
            }
        }
    }
}
