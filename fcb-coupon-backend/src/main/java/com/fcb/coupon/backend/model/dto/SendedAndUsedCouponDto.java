package com.fcb.coupon.backend.model.dto;

import lombok.Data;

/**
 * @author mashiqiong
 * @date 2021-6-22 20:39
 */
@Data
public class SendedAndUsedCouponDto {
    private Long couponThemeId;
    private Integer status;
    private Integer total;
}
