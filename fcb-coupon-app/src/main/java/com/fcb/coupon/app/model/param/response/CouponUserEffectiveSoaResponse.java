package com.fcb.coupon.app.model.param.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户券信息
 *
 * @Author WeiHaiQi
 * @Date 2021-08-13 10:25
 **/
@Data
public class CouponUserEffectiveSoaResponse implements Serializable {

    private static final long serialVersionUID = -3940182146085198520L;

    @ApiModelProperty(value = "优惠券id")
    private Long couponThemeId;
    @ApiModelProperty(value = "券码id")
    private Long couponId;
    @ApiModelProperty(value = "unionId")
    private String unionId;
}
