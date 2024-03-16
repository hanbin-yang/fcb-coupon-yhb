package com.fcb.coupon.backend.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.backend.model.dto.CouponGrowingDto;
import com.fcb.coupon.backend.service.CouponGrowingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @Description 监听发券、券核销埋点消息
 * @author mashiqiong
 * @date 2021-8-27 9:30
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponGrowingConsumer {
    private final CouponGrowingService couponGrowingService;

    /**
     * 发券
     * @param record
     * @param ack
     */
    @KafkaListener(topics = {"GROWING_COUPON_ISSUE_TOPIC"})
    public void growingCouponIssue(ConsumerRecord<String, String> record, Acknowledgment ack) {
        //解析参数
        List<CouponGrowingDto> dtoList = null;
        try {
            String value = record.value();
            dtoList = JSON.parseArray(value, CouponGrowingDto.class);
        } catch (Exception ex) {
            log.error("接收发券埋点的kafka消息：解析异常", ex);
            ack.acknowledge();
            return;
        }

        //判断参数
        if (Objects.isNull(dtoList)) {
            log.error("接收发券埋点的kafka消息：参数错误，dto=null");
            ack.acknowledge();
            return;
        }

        try {
            couponGrowingService.growingCouponsIssue(dtoList);
        } catch (Exception ex) {
            log.error("接收发券埋点的kafka消息，Exception:", ex);
        } finally {
            log.info("接收发券埋点的kafka消息：处理成功");
            ack.acknowledge();
        }
    }

    /**
     * 券核
     * @param record
     * @param ack
     */
    @KafkaListener(topics = {"GROWING_COUPON_VERIFICATION_TOPIC"})
    public void growingCouponVerification(ConsumerRecord<String, String> record, Acknowledgment ack) {
        //解析参数
        List<CouponGrowingDto> dtoList = null;
        try {
            String value = record.value();
            dtoList = JSON.parseArray(value, CouponGrowingDto.class);
        } catch (Exception ex) {
            log.error("接收券核销埋点的kafka消息：解析异常", ex);
            ack.acknowledge();
            return;
        }

        //判断参数
        if (dtoList == null) {
            log.error("接收券核销埋点的kafka消息：参数错误，dto=null");
            ack.acknowledge();
            return;
        }

        try {
            couponGrowingService.growingCouponsVerification(dtoList);
        } catch (Exception ex) {
            log.error("接收券核销埋点的kafka消息，Exception:", ex);
        } finally {
            log.info("接收券核销埋点的kafka消息：处理成功");
            ack.acknowledge();
        }
    }
}
