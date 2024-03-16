package com.fcb.coupon.app.model.param.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 查询用户优惠券信息
 *
 * @Author WeiHaiQi
 * @Date 2021-08-23 16:40
 **/
@Data
public class CouponUserEffectiveTotalResponse implements Serializable {

    private static final long serialVersionUID = 3347999054357124169L;

    @ApiModelProperty(value = "用户有效券总数")
    private Integer userValidCoupons;
}
