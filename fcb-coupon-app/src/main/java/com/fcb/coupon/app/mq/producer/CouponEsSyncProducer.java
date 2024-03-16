package com.fcb.coupon.app.mq.producer;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class CouponEsSyncProducer {

    private static String TOPIC = "coupon_sync_es";

    @Resource
    private KafkaTemplate kafkaTemplate;


    /*
    推送同步es的消息到kafka
     */
    public void sendSyncEsMessage(List<Long> couponIds) {
        if (CollectionUtils.isEmpty(couponIds)) {
            return;
        }
        String message = JSON.toJSONString(couponIds);
        try {
            kafkaTemplate.send(TOPIC, message);
        } catch (Exception ex) {
            log.error("推送同步es的消息到kafka异常,参数={}", message, ex);
        }
    }
}
