package com.fcb.coupon.backend.mq.producer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fcb.coupon.backend.model.dto.MessageReceiverDto;
import com.fcb.coupon.common.enums.NotifyTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 唐陆军
 * @Description 消息生产者
 * @createTime 2021年08月06日 15:21:00
 */
@Slf4j
@Component
public class MessageProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Value("${spring.application.name}")
    private String applicationName;

    /*
     * @description 发送短信
     * @author 唐陆军

     * @param: event
     * @param: receivers
     * @param: param
     * @date 2021-8-6 16:31
     */
    public void send(String event, List<MessageReceiverDto> receivers, Map<String, String> param, List<NotifyTypeEnum> notifyTypeEnums) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("event", event);
            jsonObject.put("kfSender", applicationName);
            jsonObject.put("receiver", receivers);

            Map<String, Map<String, String>> paramMap = new HashMap();
            for (NotifyTypeEnum notifyTypeEnum : notifyTypeEnums) {
                paramMap.put(notifyTypeEnum.getKey(), param);
            }
            jsonObject.put("messageParam", paramMap);
            String message = jsonObject.toJSONString();
            log.info("发送短信消息:{}", message);
            kafkaTemplate.send("SHUNT_TRIGGER_EVENTSYSTEM_TOPIC", message);
            log.info("发送短信消息成功");
        } catch (Exception ex) {
            log.error("发送短信消息异常", ex);
        }
    }


}
