package com.fcb.coupon.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.backend.model.dto.CouponUserConditionDto;
import com.fcb.coupon.backend.model.dto.CouponUserTotalDto;
import com.fcb.coupon.backend.model.dto.SendedAndUsedCouponDto;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;

import java.util.List;
import java.util.Map;

/**
 * 券用户服务
 *
 * @Author WeiHaiQi
 * @Date 2021-06-22 10:21
 **/
public interface CouponUserService extends IService<CouponUserEntity> {


    List<SendedAndUsedCouponDto> countSendedAndUsedCoupons(Map<String, Object> params);

    void batchSave(List<CouponUserEntity> couponUserEntities);

}
