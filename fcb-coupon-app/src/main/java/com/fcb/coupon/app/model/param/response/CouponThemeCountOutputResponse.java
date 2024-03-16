package com.fcb.coupon.app.model.param.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 优惠券活动券数量
 *
 * @Author WeiHaiQi
 * @Date 2021-08-20 18:05
 **/
@Data
@ApiModel(value = "优惠券活动库存数量出参")
public class CouponThemeCountOutputResponse implements Serializable {

    @ApiModelProperty(value = "券活动ID")
    private Long couponThemeId;
    @ApiModelProperty(value = "券活动剩余可发券数量")
    private Integer stockCount = 0;
}
