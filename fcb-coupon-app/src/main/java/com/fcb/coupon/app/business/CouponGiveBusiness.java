package com.fcb.coupon.app.business;

import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponGiveEntity;

/**
 * @author 唐陆军
 * @Description 优惠券转赠业务
 * @createTime 2021年08月12日 09:46:00
 */
public interface CouponGiveBusiness {


    /*
     * 领券转赠优惠券
     */
    void receive(Long beforeGiveId, String receiveUserId);
}
