package com.fcb.coupon.common.log;

import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.util.UUIDUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.MDC;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.converter.MessagingMessageConverter;
import org.springframework.messaging.Message;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * @author 唐陆军
 * @Description kafka消息预处理
 * @createTime 2021年08月17日 16:38:00
 */
public class KafkaMessagingMessageConverter extends MessagingMessageConverter {

    @Override
    public Message<?> toMessage(ConsumerRecord<?, ?> record, Acknowledgment acknowledgment, Consumer<?, ?> consumer, Type type) {
        Message<?> message = super.toMessage(record, acknowledgment, consumer, type);
        // 从请求头中获取traceId
        String traceId = null;

        Object traceIdObj = message.getHeaders().get(InfraConstant.TRACE_ID);
        if (traceIdObj != null && StringUtils.isBlank(traceIdObj.toString())) {
            traceId = traceIdObj.toString();
        } else {
            //不存在就生成一个
            traceId = UUIDUtils.getShortUUID();
        }
        //kafka消费专用线程，每次都是覆盖，不会清理traceId
        MDC.put(InfraConstant.TRACE_ID, traceId);

        return message;
    }

    @Override
    public ProducerRecord<?, ?> fromMessage(Message<?> message, String defaultTopic) {
        ProducerRecord<?, ?> producerRecord = super.fromMessage(message, defaultTopic);
        String traceId = MDC.get(InfraConstant.TRACE_ID);
        if (StringUtils.isNotBlank(traceId)) {
            producerRecord.headers().add(InfraConstant.TRACE_ID, traceId.getBytes(StandardCharsets.UTF_8));
        }
        return producerRecord;
    }
}