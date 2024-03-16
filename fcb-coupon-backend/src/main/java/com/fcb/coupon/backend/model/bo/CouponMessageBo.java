package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;
import lombok.Data;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月06日 16:45:00
 */
@Data
public class CouponMessageBo {

    private CouponEntity couponEntity;

    private CouponUserEntity couponUserEntity;
}
