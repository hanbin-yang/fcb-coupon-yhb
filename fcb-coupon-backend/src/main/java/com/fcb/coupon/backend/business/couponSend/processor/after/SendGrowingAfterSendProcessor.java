package com.fcb.coupon.backend.business.couponSend.processor.after;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.backend.business.couponSend.AfterSendProcessor;
import com.fcb.coupon.backend.model.dto.CouponGrowingDto;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 马仕琼
 * @Description 发送kafka埋点信息处理
 * @createTime 2021年08月27日 19:05:00
 */
@Component
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class SendGrowingAfterSendProcessor implements AfterSendProcessor {

    private final KafkaTemplate kafkaTemplate;

    @Override
    public void process(List<CouponSendContext> sendCouponContexts, CouponThemeEntity couponTheme) {
        //发送kafka埋点信息
        List<CouponGrowingDto> dtoList = new ArrayList<>();
        for (CouponSendContext couponSendContext : sendCouponContexts) {
            CouponGrowingDto dto = new CouponGrowingDto();
            dto.setCouponThemeId(couponTheme.getId());
            dto.setThemeTitle(couponTheme.getThemeTitle());
            dto.setUserType(couponSendContext.getUserType());
            dto.setUserId(couponSendContext.getUserId());
            dto.setBindTime(couponSendContext.getCouponUserEntity().getCreateTime());
            dtoList.add(dto);
        }

        if (CollectionUtils.isEmpty(dtoList)) {
            return;
        }

        this.sendGrowingMessage(dtoList);
    }

    /**
     * 埋点
     * @param dtoList
     */
    private void sendGrowingMessage(List<CouponGrowingDto> dtoList) {
        try {
            String jsonString = JSON.toJSONString(dtoList);
            log.info("发券埋点发kafa消息data:" + jsonString);
            kafkaTemplate.send("GROWING_COUPON_VERIFICATION_TOPIC", jsonString);
            log.info("发券埋点发kafa消息data end");
        } catch (Exception ex) {
            log.error("发券埋点发kafa消息发送出错:", ex);
        }
    }
}
