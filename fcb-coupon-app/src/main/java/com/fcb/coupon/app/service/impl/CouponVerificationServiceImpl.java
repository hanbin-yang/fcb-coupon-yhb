package com.fcb.coupon.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.app.exception.Coupon4OrderErrorCode;
import com.fcb.coupon.app.mapper.CouponVerificationMapper;
import com.fcb.coupon.app.model.bo.OperateCouponDbBo;
import com.fcb.coupon.app.model.dto.CouponDo;
import com.fcb.coupon.app.model.entity.CouponVerificationEntity;
import com.fcb.coupon.app.service.CouponService;
import com.fcb.coupon.app.service.CouponVerificationService;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.enums.CouponStatusEnum;
import com.fcb.coupon.common.enums.YesNoEnum;
import com.fcb.coupon.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HanBin_Yang
 * @since 2021/6/23 9:16
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@RefreshScope
public class CouponVerificationServiceImpl extends ServiceImpl<CouponVerificationMapper, CouponVerificationEntity> implements CouponVerificationService {
    private final CouponService couponService;
    //private final CouponEsDocService couponEsDocService;

    @Resource(name = "couponVerificationOprExecutor")
    private ThreadPoolTaskExecutor couponVerificationOprExecutor;

    @Override
    @Transactional
    public void operateCoupon4OrderWithTx(OperateCouponDbBo bo) {
        //List<CouponEsDoc> esDocList = new ArrayList<>();
        // 解锁
        if (CollectionUtils.isNotEmpty(bo.getUnlockCoupons())) {
            List<Long> unlockIds = unlockCoupons(bo);
            // 准备es bean
            //esDocList.addAll(prepareEsBeans(bo, unlockIds, CouponStatusEnum.STATUS_USE));
        }
        // 换绑
        if (CollectionUtils.isNotEmpty(bo.getRebindCoupons())) {
            List<Long> rebindIds = rebindCoupons(bo);
            //esDocList.addAll(prepareEsBeans(bo, rebindIds, null));
        }
        // 上锁
        if (CollectionUtils.isNotEmpty(bo.getLockCoupons())) {
            List<Long> lockIds = lockCoupons(bo);
            //esDocList.addAll(prepareEsBeans(bo, lockIds, CouponStatusEnum.STATUS_LOCKED));
        }
//        if (CollectionUtils.isNotEmpty(esDocList)) {
//            String traceId = MDC.get(InfraConstant.TRACE_ID);
//            couponVerificationOprExecutor.execute(() -> {
//                try {
//                    MDC.put(InfraConstant.TRACE_ID, traceId);
//                    //couponEsDocService.updateBatch(esDocList);
//                } finally {
//                    MDC.remove(InfraConstant.TRACE_ID);
//                }
//            });
//            return ;
//        }
        // 核销
        if (CollectionUtils.isNotEmpty(bo.getVerifyCoupons())) {
            List<Long> verifyIds = verifyCoupons(bo);
            //esDocList.addAll(prepareEsBeans(bo, verifyIds, CouponStatusEnum.STATUS_USED));
            // 同步es
            String traceId = MDC.get(InfraConstant.TRACE_ID);
            couponVerificationOprExecutor.execute(() -> {
                try {
                    MDC.put(InfraConstant.TRACE_ID, traceId);
                    //couponEsDocService.updateBatch(esDocList);
                } finally {
                    MDC.remove(InfraConstant.TRACE_ID);
                }
            });
        }
    }

//    private List<CouponEsDoc> prepareEsBeans(OperateCouponDbBo bo, List<Long> ids, CouponStatusEnum statusToUse) {
//        List<CouponEsDoc> esDocList  = new ArrayList<>();
//        for (Long id : ids) {
//            CouponEsDoc couponEsDoc = new CouponEsDoc();
//            couponEsDoc.setId(id);
//            couponEsDoc.setUpdateTime(bo.getUpdateTime());
//            couponEsDoc.setBindTel(bo.getBindTel());
//            if (statusToUse != null) {
//                couponEsDoc.setStatus(statusToUse.getStatus());
//            }
//
//            esDocList.add(couponEsDoc);
//        }
//        return esDocList;
//    }

    private List<Long> verifyCoupons(OperateCouponDbBo bo) {
        List<CouponDo> verifyCoupons = bo.getVerifyCoupons();
        List<Long> verifyIds = verifyCoupons.stream().map(CouponDo::getId).collect(Collectors.toList());
        // 核销coupon表
        int verifyResult = couponService.updateStatusBatchByIds(verifyIds, Collections.singletonList(CouponStatusEnum.STATUS_LOCKED.getStatus()), CouponStatusEnum.STATUS_USED.getStatus());
        if (verifyResult != verifyIds.size()) {
            log.error("couponDAO核销优惠券verifyCoupons4Order error: needToOprSize={}, verifyResultSize={}, verifyCoupons={}", verifyIds.size(), verifyResult, JSON.toJSONString(verifyCoupons));
            throw new BusinessException(Coupon4OrderErrorCode.VERIFY_COUPON_DB_ERROR);
        }

        // 核销coupon_verification表
        CouponVerificationEntity verificationEntity = getCouponVerificationEntity(bo);
        verificationEntity.setStatus(CouponStatusEnum.STATUS_USED.getStatus());
        verificationEntity.setUserType(bo.getVerifyCoupons().get(0).getUserType());
        baseMapper.updateBatchByIds(verifyIds, verificationEntity);
        return verifyIds;
    }

    private List<Long> lockCoupons(OperateCouponDbBo bo) {
        List<Long> lockIds = bo.getLockCoupons().stream().map(CouponDo::getId).collect(Collectors.toList());
        // 上锁coupon表
        int lockResult = couponService.updateStatusBatchByIds(lockIds, Collections.singletonList(CouponStatusEnum.STATUS_USE.getStatus()), CouponStatusEnum.STATUS_LOCKED.getStatus());
        if (lockResult != lockIds.size()) {
            log.error("couponDAO上锁优惠券lockCoupons4Order error: needToOprSize={}, lockResultSize={}, lockCoupons={}", lockIds.size(), lockResult, JSON.toJSONString(bo.getLockCoupons()));
            throw new BusinessException(Coupon4OrderErrorCode.LOCK_COUPON_DB_ERROR);
        }

        List<CouponVerificationEntity> verificationLockBeans = new ArrayList<>();
        for (CouponDo item : bo.getLockCoupons()) {
            CouponVerificationEntity verificationEntity = getCouponVerificationEntity(bo, item);
            verificationEntity.setStatus(CouponStatusEnum.STATUS_LOCKED.getStatus());
            verificationLockBeans.add(verificationEntity);
        }
        // 上锁coupon_verification表
        baseMapper.insertOrUpdateBatch(verificationLockBeans);
        return lockIds;
    }

    private List<Long> rebindCoupons(OperateCouponDbBo bo) {
        List<CouponDo> rebindCoupons = bo.getRebindCoupons();
        List<Long> rebindIds = rebindCoupons.stream().map(CouponDo::getId).collect(Collectors.toList());
        // 换绑coupon_verification表
        CouponVerificationEntity couponVerificationEntity = new CouponVerificationEntity();
        couponVerificationEntity
                .setUsedStoreId(bo.getStoreId())
                .setUsedStoreCode(bo.getBuildCode())
                .setUsedStoreName(bo.getStoreName())
                .setUsedRoomGuid(bo.getRoomGuid())
                .setProductCode(bo.getRoomGuid())
                .setProductName(bo.getRoomName())
                .setBindTel(bo.getBindTel())
                .setOrderCode(bo.getOrderCode())
        ;
        int rebindVResult = baseMapper.rebindCoupons(rebindIds, couponVerificationEntity);
        if (rebindVResult != rebindIds.size()) {
            log.error("couponVerificationDao换绑优惠券rebindCoupons4Order error: needToOprSize={}, rebindResultSize={}, rebindCoupons={}", rebindIds.size(), rebindIds, JSON.toJSONString(rebindCoupons));
            throw new BusinessException(Coupon4OrderErrorCode.REBIND_COUPON_DB_ERROR);
        }
        return rebindIds;
    }

    private List<Long> unlockCoupons(OperateCouponDbBo bo) {
        // 正式解锁优惠券
        List<Long> unlockIds = bo.getUnlockCoupons().stream().map(CouponDo::getId).collect(Collectors.toList());
        // 解锁coupon表
        int unlockCouponResult = couponService.updateStatusBatchByIds(unlockIds, Collections.singletonList(CouponStatusEnum.STATUS_LOCKED.getStatus()), CouponStatusEnum.STATUS_USE.getStatus());
        if (unlockCouponResult != unlockIds.size()) {
            log.error("couponDAO解锁优惠券unlockCoupons4Order unlockCouponResult error: needToOprSize={}, unlockResultSize={}, unlockCoupons={}", unlockIds.size(), unlockCouponResult, JSON.toJSONString(bo.getUnlockCoupons()));
            throw new BusinessException(Coupon4OrderErrorCode.UNLOCK_DB_ERROR);
        }
        // 解锁coupon_verification表
        int unlockVerificationResult = baseMapper.unlockCoupons(unlockIds);
        if (unlockVerificationResult != unlockIds.size()) {
            log.error("couponDAO解锁优惠券unlockCoupons4Order unlockVerificationResult error: needToOprSize={}, unlockResultSize={}, unlockCoupons={}", unlockIds.size(), unlockVerificationResult, JSON.toJSONString(bo.getUnlockCoupons()));
            throw new BusinessException(Coupon4OrderErrorCode.UNLOCK_DB_ERROR);
        }
        return unlockIds;
    }

    private CouponVerificationEntity getCouponVerificationEntity(OperateCouponDbBo bo, CouponDo couponDo) {
        CouponVerificationEntity verificationEntity = getCouponVerificationEntity(bo);
        verificationEntity.setCouponId(couponDo.getId());
        verificationEntity.setCouponThemeId(couponDo.getCouponThemeId());
        verificationEntity.setCouponCode(couponDo.getCouponCode());
        verificationEntity.setCreateUserid(couponDo.getCouponCreateUserid());
        verificationEntity.setCreateUsername(couponDo.getCouponCreateUsername());
        verificationEntity.setThemeTitle(couponDo.getThemeTitle());
        verificationEntity.setBindUserId(couponDo.getUserId());
        verificationEntity.setUserType(couponDo.getUserType());
        verificationEntity.setStartTime(couponDo.getStartTime());
        verificationEntity.setEndTime(couponDo.getEndTime());
        verificationEntity.setCouponCreateTime(couponDo.getCouponCreateTime());
        verificationEntity.setIsDeleted(YesNoEnum.NO.getValue());
        verificationEntity.setCouponDiscountType(couponDo.getCouponDiscountType());
        verificationEntity.setCouponValue(couponDo.getCouponValue());
        return verificationEntity;
    }

    private CouponVerificationEntity getCouponVerificationEntity(OperateCouponDbBo bo) {
        CouponVerificationEntity verificationEntity = new CouponVerificationEntity();
        verificationEntity.setSubscribeCode(bo.getOrderCode());
        verificationEntity.setOrderCode(bo.getOrderCode());
        verificationEntity.setUsedStoreId(bo.getStoreId());
        verificationEntity.setUsedStoreCode(bo.getBuildCode());
        verificationEntity.setUsedStoreName(bo.getStoreName());
        verificationEntity.setUsedRoomGuid(bo.getRoomGuid());
        verificationEntity.setProductName(bo.getRoomName());
        verificationEntity.setProductCode(bo.getRoomGuid());
        verificationEntity.setUsedChannel(bo.getUsedChannel());
        verificationEntity.setUsedTime(bo.getUpdateTime());
        verificationEntity.setBindTel(bo.getBindTel());
        return verificationEntity;
    }
}
