package com.fcb.coupon.backend.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.backend.business.couponSend.CouponSendBusiness;
import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.model.bo.CouponBatchSendBo;
import com.fcb.coupon.backend.model.bo.CouponSendUserBo;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.dto.CouponSendResult;
import com.fcb.coupon.backend.model.mongo.MktTaskRunNodeUserEntity;
import com.fcb.coupon.backend.model.param.request.CouponSendDetailMessageRequest;
import com.fcb.coupon.backend.model.param.request.CouponSendMessageRequest;
import com.fcb.coupon.backend.model.param.request.CouponSendUserRequest;
import com.fcb.coupon.backend.mongo.MktTaskRunNodeUserRepository;
import com.fcb.coupon.common.enums.CouponSourceTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 唐陆军
 * @Description 监听主动营销发券消息
 * @createTime 2021年06月09日 20:04:00
 */
@Slf4j
@Component
public class SendMktCouponConsumer {

    //-1未发送
    private static final Integer RUN_NODE_USER_STATUS_INITIAL = -1;
    //1发券成功
    private static final Integer RUN_NODE_USER_STATUS_SUCCESS = 1;
    //0发券返回null
    private static final Integer RUN_NODE_USER_STATUS_FAILURE = 0;
    //2发券返回每个人单个券领取数量超过限制;
    private static final Integer RUN_NODE_USER_STATUS_EVERYBODY_LIMIT = 2;
    //3发券返回超过活动最大限制;
    private static final Integer RUN_NODE_USER_STATUS_MAX_COUPON_LIMIT = 3;
    //4发券抛出Exception;
    private static final Integer RUN_NODE_USER_STATUS_EXCEPTION = 4;
    //5活动任务已结束;
    private static final Integer RUN_NODE_USER_STATUS_TASK_STOP = 5;
    //6券面额信息有误;
    private static final Integer RUN_NODE_USER_STATUS_DENOMINATION_ERROR = 6;
    //7参数不合法
    private static final Integer RUN_NODE_USER_STATUS_INVALID_PARAMETER = 7;

    @Autowired
    private CouponSendBusiness couponSendBusiness;

    @Autowired
    private MktTaskRunNodeUserRepository mktTaskRunNodeUserRepository;

    @KafkaListener(topics = {"SEND_COUPON_TOPIC"})
    public void sendCoupon(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("接收发送优惠券的kafka消息：{}", record.toString());

        //解析参数
        CouponSendMessageRequest request = null;
        try {
            String value = record.value();
            request = JSON.parseObject(value, CouponSendMessageRequest.class);
        } catch (Exception ex) {
            log.error("接收发送优惠券的kafka消息：解析异常", ex);
            ack.acknowledge();
            return;
        }

        CouponSendDetailMessageRequest detailRequest = request.getData();
        //判断参数
        if (detailRequest == null) {
            log.error("接收发送优惠券的kafka消息：参数错误，data=null");
            ack.acknowledge();
            return;
        }
        if (detailRequest.getCouponThemeId() == null) {
            log.error("接收发送优惠券的kafka消息：参数错误，couponThemeId=null");
            ack.acknowledge();
            return;
        }
        if (CollectionUtils.isEmpty(detailRequest.getSendUsers())) {
            log.error("接收发送优惠券的kafka消息：参数错误，发送对象为空");
            ack.acknowledge();
            return;
        }

        //构造业务处理对象
        CouponBatchSendBo couponBatchSendBo = buildCouponBatchBo(detailRequest);

        //发送优惠券
        try {
            //发券
            CouponSendResult couponSendResult = couponSendBusiness.marketingBatchSend(couponBatchSendBo);
            //同步CRM主动营销发券结果
            syncSendResult(couponSendResult);
        } catch (BusinessException ex) {
            syncBusinessExceptionResult(detailRequest.getSendUsers(), ex);
        } catch (Exception ex) {
            log.error("接收发送优惠券的kafka消息：发券异常", ex);
            syncExceptionResult(detailRequest.getSendUsers(), RUN_NODE_USER_STATUS_EXCEPTION, "发券异常");
        } finally {
            ack.acknowledge();
            log.info("接收发送优惠券的kafka消息：处理成功");
        }
    }


    /*
     * @description 校验重复
     * @author 唐陆军
     * @date 2021-8-23 10:51
     */
    private CouponBatchSendBo buildCouponBatchBo(CouponSendDetailMessageRequest request) {
        List<CouponSendUserBo> canSendUsers = new ArrayList<>();
        for (CouponSendUserRequest sendUser : request.getSendUsers()) {
            CouponSendUserBo couponSendUserBo = new CouponSendUserBo();
            couponSendUserBo.setUserId(sendUser.getUserId());
            couponSendUserBo.setCount(1);
            couponSendUserBo.setTransactionId(sendUser.getMongoId());
            canSendUsers.add(couponSendUserBo);
        }
        CouponBatchSendBo couponBatchSendBo = new CouponBatchSendBo();
        couponBatchSendBo.setThemeId(request.getCouponThemeId());
        couponBatchSendBo.setSource(CouponSourceTypeEnum.COUPON_SOURCE_MARKTEING_VOUCHER.getSource());
        couponBatchSendBo.setSourceId(request.getSourceId());
        couponBatchSendBo.setSendUserType(request.getSendCouponUserType());
        couponBatchSendBo.setSendUserList(canSendUsers);
        return couponBatchSendBo;
    }

    /*
     * @description 同步发送结果
     * @author 唐陆军
     * @date 2021-8-23 10:55
     */
    private void syncSendResult(CouponSendResult sendResult) {
        List<MktTaskRunNodeUserEntity> mongoEntities = new ArrayList<>();
        for (CouponSendContext sendContext : sendResult.getSendContexts()) {
            MktTaskRunNodeUserEntity mongoEntity = mktTaskRunNodeUserRepository.findByMongoId(sendContext.getSourceId());
            if (mongoEntity == null) {
                continue;
            }
            //发券成功
            if (Boolean.FALSE.equals(sendContext.getIsFailure())) {
                mongoEntity.setStatus(RUN_NODE_USER_STATUS_SUCCESS);
                mongoEntity.setMessage(sendContext.getFailureReason());
                mongoEntities.add(mongoEntity);
                continue;
            }

            //发送失败
            if (Boolean.TRUE.equals(sendContext.getCanRetry())) {
                //能重试
                mongoEntity.setStatus(RUN_NODE_USER_STATUS_EXCEPTION);
                mongoEntity.setMessage(sendContext.getFailureReason());
                mongoEntities.add(mongoEntity);
            } else {
                mongoEntity.setStatus(RUN_NODE_USER_STATUS_EVERYBODY_LIMIT);
                mongoEntity.setMessage(sendContext.getFailureReason());
                mongoEntities.add(mongoEntity);
            }
        }
        mktTaskRunNodeUserRepository.saveAll(mongoEntities);
    }

    private void syncBusinessExceptionResult(List<CouponSendUserRequest> canSendUsers, BusinessException ex) {
        Integer status = getStatus(ex);
        syncExceptionResult(canSendUsers, status, ex.getMessage());
    }

    private void syncExceptionResult(List<CouponSendUserRequest> canSendUsers, Integer status, String message) {
        List<MktTaskRunNodeUserEntity> mongoEntities = new ArrayList<>();
        for (CouponSendUserRequest canSendUser : canSendUsers) {
            MktTaskRunNodeUserEntity mongoEntity = mktTaskRunNodeUserRepository.findByMongoId(canSendUser.getMongoId());
            if (mongoEntity == null) {
                continue;
            }
            mongoEntity.setStatus(status);
            mongoEntity.setMessage(message);
            mongoEntities.add(mongoEntity);
        }
        mktTaskRunNodeUserRepository.saveAll(mongoEntities);
    }


    private Integer getStatus(BusinessException ex) {
        if (CouponThemeErrorCode.COUPON_THEME_NOT_EXIST.getCode().equals(ex.getCode()) ||
                CouponThemeErrorCode.COUPON_THEME_TYPE_ERROR.getCode().equals(ex.getCode()) ||
                CouponThemeErrorCode.COUPON_THEME_SEND_TYPE_NOT_SUPPORT.getCode().equals(ex.getCode()) ||
                CouponThemeErrorCode.COUPON_THEME_NOT_EFFECTIVE.getCode().equals(ex.getCode()) ||
                CouponThemeErrorCode.COUPON_THEME_NOT_CONFIG_SEND_USER.getCode().equals(ex.getCode()) ||
                CouponThemeErrorCode.COUPON_THEME_SEND_USER_NOT_MATCH.getCode().equals(ex.getCode()) ||
                CouponThemeErrorCode.COUPON_THEME_SEND_COUNT_ERROR.getCode().equals(ex.getCode()) ||
                CouponThemeErrorCode.COUPON_THEME_SEND_USER_REPEAT.getCode().equals(ex.getCode())) {
            //参数错误
            return RUN_NODE_USER_STATUS_INVALID_PARAMETER;
        } else if (CouponThemeErrorCode.COUPON_THEME_CAN_SEND_COUNT_ERROR.getCode().equals(ex.getCode())) {
            //发券返回超过活动最大限制;
            return RUN_NODE_USER_STATUS_MAX_COUPON_LIMIT;
        } else if (CouponThemeErrorCode.COUPON_THEME_NOT_IN_START_END_TIME.getCode().equals(RUN_NODE_USER_STATUS_TASK_STOP)) {
            //活动时间错误
            return RUN_NODE_USER_STATUS_TASK_STOP;
        } else {
            return RUN_NODE_USER_STATUS_EXCEPTION;
        }
    }

}
