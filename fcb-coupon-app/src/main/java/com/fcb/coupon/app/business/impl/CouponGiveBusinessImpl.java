package com.fcb.coupon.app.business.impl;

import com.fcb.coupon.app.business.CouponGiveBusiness;
import com.fcb.coupon.app.exception.CouponGiveErrorCode;
import com.fcb.coupon.app.model.dto.CouponBeforeGiveCacheDto;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponGiveEntity;
import com.fcb.coupon.app.model.entity.CouponUserEntity;
import com.fcb.coupon.app.mq.producer.CouponEsSyncProducer;
import com.fcb.coupon.app.remote.dto.input.CustomerIdInput;
import com.fcb.coupon.app.remote.dto.output.CustomerIdInfoOutput;
import com.fcb.coupon.app.remote.user.CustomerFeignClient;
import com.fcb.coupon.app.service.CouponBeforeGiveCacheService;
import com.fcb.coupon.app.service.CouponGiveService;
import com.fcb.coupon.app.service.CouponService;
import com.fcb.coupon.app.service.CouponUserService;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.CouponSourceTypeEnum;
import com.fcb.coupon.common.enums.CouponStatusEnum;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.RedisUtil;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月26日 18:49:00
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponGiveBusinessImpl implements CouponGiveBusiness {
    private final CouponBeforeGiveCacheService couponBeforeGiveCacheService;
    private final CouponService couponService;
    private final CustomerFeignClient customerFeignClient;
    private final CouponGiveService couponGiveService;
    private final CouponUserService couponUserService;
    private final CouponEsSyncProducer couponEsSyncProducer;

    /*
     * 领券转赠优惠券
     */
    @Override
    public void receive(Long beforeGiveId, String receiveUserId) {
        //查询转赠记录信息
        CouponBeforeGiveCacheDto beforeGiveCacheDto = couponBeforeGiveCacheService.getById(beforeGiveId);
        checkBeforeGive(beforeGiveCacheDto);

        //查询转赠用户券信息
        CouponEntity giveCoupon = couponService.getById(beforeGiveCacheDto.getCouponId());
        checkGiveCoupon(giveCoupon, beforeGiveCacheDto, receiveUserId);

        CouponUserEntity giveUserEntity = couponUserService.get(giveCoupon.getUserId(), giveCoupon.getUserType(), giveCoupon.getId());
        if (giveUserEntity == null) {
            log.warn("无领券信息记录，请检查脏数据，couponId={}", beforeGiveCacheDto.getCouponId());
            throw new BusinessException(CouponGiveErrorCode.GIVE_COUPON_CHANGE_ERROR);
        }

        //查询用户信息
        CustomerIdInfoOutput customer = getCustomerInfo(Long.valueOf(receiveUserId));
        if (customer == null) {
            throw new BusinessException(CouponGiveErrorCode.GIVE_RECEIVE_USER_ERROR);
        }

        //构建接收人券信息
        CouponEntity receiveCoupon = new CouponEntity();
        BeanUtils.copyProperties(giveCoupon, receiveCoupon);
        receiveCoupon.setId(RedisUtil.generateId());
        receiveCoupon.setSource(CouponSourceTypeEnum.COUPON_SOURCE_VIDEO_LIVE.getSource());
        receiveCoupon.setUserId(receiveUserId);
        receiveCoupon.setUserType(UserTypeEnum.C.getUserType());
        receiveCoupon.setCreateTime(new Date());

        CouponUserEntity couponUserEntity = new CouponUserEntity();
        couponUserEntity.setCouponId(receiveCoupon.getId());
        couponUserEntity.setCouponThemeId(receiveCoupon.getCouponThemeId());
        couponUserEntity.setBindTel(customer.getPhone());
        couponUserEntity.setStatus(receiveCoupon.getStatus());
        couponUserEntity.setUserId(receiveUserId);
        couponUserEntity.setUserType(receiveCoupon.getUserType());
        couponUserEntity.setCreateTime(receiveCoupon.getCreateTime());
        couponUserEntity.setEndTime(giveCoupon.getEndTime());

        CouponGiveEntity couponGiveEntity = new CouponGiveEntity();
        couponGiveEntity.setCouponId(giveCoupon.getId());
        couponGiveEntity.setCouponThemeId(giveCoupon.getCouponThemeId());
        couponGiveEntity.setGiveType(1);
        couponGiveEntity.setGiveUserId(giveCoupon.getUserId());
        couponGiveEntity.setGiveUserMobile(giveUserEntity.getBindTel());
        couponGiveEntity.setGiveTime(new Date());
        couponGiveEntity.setReceiveUserId(couponUserEntity.getUserId());
        couponGiveEntity.setReceiveUserMobile(couponUserEntity.getBindTel());
        couponGiveEntity.setReceiveCouponId(receiveCoupon.getId());
        couponGiveEntity.setReceiveUserType(receiveCoupon.getUserType());

        couponGiveService.receiveGiveCoupon(giveCoupon, receiveCoupon, couponUserEntity, couponGiveEntity);


        //更新转赠缓存信息
        try {
            couponBeforeGiveCacheService.refreshCouponBeforeGiveCache(beforeGiveId);
        } catch (Exception ex) {
            log.error("刷新转赠缓存异常", ex);
        }

        //同步ES
        couponEsSyncProducer.sendSyncEsMessage(Lists.newArrayList(giveCoupon.getId(), receiveCoupon.getId()));
    }


    /*
     * @description 校验转增前的信息
     * @author 唐陆军
     * @param: beforeGiveCacheDto
     * @date 2021-8-26 14:19
     */
    private void checkBeforeGive(CouponBeforeGiveCacheDto beforeGiveCacheDto) {
        if (beforeGiveCacheDto == null) {
            throw new BusinessException(CouponGiveErrorCode.GIVE_NOT_FOUND_ERROR);
        }
        Date now = new Date();
        if (now.after(beforeGiveCacheDto.getExpireTime())) {
            throw new BusinessException(CouponGiveErrorCode.GIVE_EXPIRE_ERROR);
        }
    }


    private void checkGiveCoupon(CouponEntity giveCoupon, CouponBeforeGiveCacheDto beforeGiveCacheDto, String receiveUserId) {
        if (giveCoupon == null) {
            throw new BusinessException(CouponGiveErrorCode.GIVE_COUPON_NOT_FOUND_ERROR);
        }
        if (new Date().after(giveCoupon.getEndTime())) {
            throw new BusinessException(CouponGiveErrorCode.GIVE_COUPON_EXPIRE_ERROR);
        }

        //优惠券是已转赠状态
        if (CouponStatusEnum.STATUS_DONATE.getStatus().equals(giveCoupon.getStatus())) {
            //查询领券信息
            CouponGiveEntity couponGiveEntity = couponGiveService.getById(giveCoupon.getId());
            if (couponGiveEntity == null) {
                log.error("券是转增状态但无转赠记录，是异常数据，couponId={}", beforeGiveCacheDto.getCouponId());
                throw new BusinessException(CouponGiveErrorCode.GIVE_COUPON_CHANGE_ERROR);
            }
            //领取人是自己
            if (receiveUserId.equals(couponGiveEntity.getReceiveUserId())) {
                throw new BusinessException(CouponGiveErrorCode.GIVE_NOT_REPEAT_RECEIVED_ERROR);
            }
            //领取人是别人
            throw new BusinessException(CouponGiveErrorCode.GIVE_OTHER_RECEIVED_ERROR);
        }
        //优惠券是非可使用
        if (!CouponStatusEnum.STATUS_USE.getStatus().equals(giveCoupon.getStatus())) {
            throw new BusinessException(CouponGiveErrorCode.GIVE_COUPON_CHANGE_ERROR);
        }
    }


    private CustomerIdInfoOutput getCustomerInfo(Long customerId) {
        CustomerIdInput customerIdInput = new CustomerIdInput();
        customerIdInput.setCustomerId(customerId);
        ResponseDto<CustomerIdInfoOutput> customerDto = customerFeignClient.getCustomerInfoByCustomerId(customerIdInput);
        if (CouponConstant.SUCCESS_CODE.equals(customerDto.getCode())) {
            return customerDto.getData();
        }
        throw new BusinessException(customerDto.getCode(), customerDto.getMessage());
    }
}
