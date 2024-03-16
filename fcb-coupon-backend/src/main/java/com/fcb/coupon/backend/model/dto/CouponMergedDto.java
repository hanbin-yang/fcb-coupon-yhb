package com.fcb.coupon.backend.model.dto;

import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponSendLogEntity;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;
import lombok.Data;

/**
 * @author 唐陆军
 * @Description 优惠券组合类
 * @createTime 2021年08月24日 10:33:00
 */
@Data
public class CouponMergedDto {

    private CouponEntity couponEntity;

    private CouponUserEntity couponUserEntity;

    private CouponSendLogEntity couponSendLogEntity;

}
