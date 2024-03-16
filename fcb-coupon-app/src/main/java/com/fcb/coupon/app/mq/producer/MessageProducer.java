package com.fcb.coupon.app.mq.producer;

import com.alibaba.fastjson.JSONObject;
import com.fcb.coupon.app.model.dto.MessageReceiverDto;
import com.fcb.coupon.common.enums.NotifyTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 唐陆军
 * @Description 消息生产者
 * @createTime 2021年08月06日 15:21:00
 */
@Slf4j
@Component
public class MessageProducer {
    @Resource
    private KafkaTemplate kafkaTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 通知 短信/站内消息/邮件
     * @param topic kafka topic
     * @param eventCode 短信事件码
     * @param notifyTypeEnums  事件类型 短信/站内消息/邮件
     * @param receiver 短信接收人信息 手机号 用户id
     * @param param param
     */
    public void send(String topic, String eventCode, List<NotifyTypeEnum> notifyTypeEnums, List<MessageReceiverDto> receiver, Map<String, String> param) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("kfSender", applicationName);
        jsonObject.put("event", eventCode);
        jsonObject.put("receiver", receiver);
        Map<String, Map<String, String>> paramMap = new HashMap<>();
        for (NotifyTypeEnum notifyTypeEnum : notifyTypeEnums) {
            paramMap.put(notifyTypeEnum.getKey(), param);
        }
        jsonObject.put("messageParam", paramMap);
        try {
            String data = jsonObject.toJSONString();
            log.info("send发送通知消息start: topic={}, data={}", topic, data);
            kafkaTemplate.send(topic, data);
            log.info("send发送通知消息success");
        } catch (Exception e) {
            log.error("sendSms发送通知消息error", e);
        }
    }
}
