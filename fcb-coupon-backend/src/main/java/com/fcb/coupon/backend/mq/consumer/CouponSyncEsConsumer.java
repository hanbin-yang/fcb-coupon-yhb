package com.fcb.coupon.backend.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;
import com.fcb.coupon.backend.service.CouponEsDocService;
import com.fcb.coupon.backend.service.CouponService;
import com.fcb.coupon.backend.service.CouponUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CouponSyncEsConsumer {

    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponUserService couponUserService;
    @Autowired
    private CouponEsDocService couponEsDocService;

    @KafkaListener(topics = {"coupon_sync_es"})
    public void sendCoupon(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("接收同步优惠券到ES的kafka消息：{}", record.toString());

        //解析参数
        List<Long> couponIds = null;
        try {
            String value = record.value();
            couponIds = JSON.parseArray(value, Long.class);
        } catch (Exception ex) {
            log.error("接收同步优惠券到ES的kafka消息：解析异常", ex);
            ack.acknowledge();
            return;
        }

        couponIds = couponIds.stream().distinct().collect(Collectors.toList());
        //发送优惠券
        try {
            List<CouponEntity> couponEntities = couponService.listByIds(couponIds);
            List<CouponUserEntity> couponUserEntities = couponUserService.listByIds(couponIds);
            Map<Long, CouponUserEntity> couponUserEntityMap = couponUserEntities.stream().collect(Collectors.toMap(m -> m.getCouponId(), m -> m));
            for (CouponEntity couponEntity : couponEntities) {
                CouponEsDoc couponEsDoc = new CouponEsDoc();
                BeanUtils.copyProperties(couponEntity, couponEsDoc);
                CouponUserEntity couponUserEntity = couponUserEntityMap.get(couponEntity.getId());
                if (couponUserEntity != null) {
                    couponEsDoc.setBindTel(couponUserEntity.getBindTel());
                    couponEsDoc.setBindTime(couponUserEntity.getCreateTime());
                }
                couponEsDocService.saveOrUpdateByVersion(couponEsDoc);
            }
        } catch (Exception ex) {
            log.error("接收同步优惠券到ES的kafka消息：处理异常", ex);
        } finally {
            ack.acknowledge();
            log.info("接收同步优惠券到ES的kafka消息：处理成功");
        }
    }

}
