package com.fcb.coupon.app.business.couponreceive;

import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.bo.CouponReceiveBo;

/**
 * @author YangHanBin
 * @date 2021-08-16 8:39
 */
public interface CouponReceiveBusiness {

    CouponEntity receive(CouponReceiveBo bo, CouponThemeCache couponThemeCache);

    CouponThemeCache getCouponThemeCache(Long couponThemeId);
}
