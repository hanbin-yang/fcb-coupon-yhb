package com.fcb.coupon.app.model.bo;

import lombok.Data;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月31日 11:10:00
 */
@Data
public class CouponUserGetBo {

    private String userId;

    private Integer userType;

    private Long couponId;
}
