package com.fcb.coupon.backend.infra.kafka;

import com.fcb.coupon.common.log.KafkaMessagingMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月17日 17:33:00
 */
@Configuration
public class KafkaAutoConfiguration {

    @Bean
    public KafkaMessagingMessageConverter kafkaMessagingMessageConverter() {
        return new KafkaMessagingMessageConverter();
    }

}
