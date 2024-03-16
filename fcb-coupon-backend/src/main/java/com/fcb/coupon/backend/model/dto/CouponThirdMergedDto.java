package com.fcb.coupon.backend.model.dto;

import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponThirdEntity;
import lombok.Data;


@Data
public class CouponThirdMergedDto {

    private CouponEntity couponEntity;

    private CouponThirdEntity couponThirdEntity;
}
