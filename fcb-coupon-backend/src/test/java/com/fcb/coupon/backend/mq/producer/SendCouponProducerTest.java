package com.fcb.coupon.backend.mq.producer;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.BaseTest;
import com.fcb.coupon.backend.model.param.request.CouponSendDetailMessageRequest;
import com.fcb.coupon.backend.model.param.request.CouponSendMessageRequest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月16日 19:38:00
 */
public class SendCouponProducerTest extends BaseTest {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Test
    public void couponSendMessageTest() throws Exception {
        CouponSendMessageRequest request = new CouponSendMessageRequest();
        CouponSendDetailMessageRequest messageRequest = new CouponSendDetailMessageRequest();
        messageRequest.setCouponThemeId(1L);
        request.setData(messageRequest);

        ListenableFuture<SendResult> future = kafkaTemplate.send("SEND_COUPON_TOPIC", JSON.toJSONString(request));
        System.out.println(future.get());

    }
}
