package com.fcb.coupon.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponGiveEntity;
import com.fcb.coupon.app.model.entity.CouponUserEntity;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月26日 11:42:00
 */
public interface CouponGiveService extends IService<CouponGiveEntity> {


    /**
     * @description 领券转增优惠券
     * @author 唐陆军
     * @param: sourceCoupon 转赠的优惠券
     * @param: receiveCoupon 接受的优惠券
     * @param: couponGiveEntity 转增记录
     * @date 2021-8-26 14:51
     */
    void receiveGiveCoupon(CouponEntity giveCoupon, CouponEntity receiveCoupon, CouponUserEntity couponUserEntity, CouponGiveEntity couponGiveEntity);

}
