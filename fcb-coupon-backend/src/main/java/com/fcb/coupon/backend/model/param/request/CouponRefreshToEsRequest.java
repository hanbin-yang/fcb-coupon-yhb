package com.fcb.coupon.backend.model.param.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * TODO
 *
 * @Author WeiHaiQi
 * @Date 2021-07-14 21:15
 **/
@Getter
@Setter
@ApiModel(description = "coupon表数据刷新到elasticsearch 入参")
public class CouponRefreshToEsRequest {
    @ApiModelProperty(value = "刷新类型标志，通过券活动id全量刷：refreshByCouponThemeId；券id刷：refreshByCouponId；全量刷：refreshAllCoupon；", required = true)
    @NotNull
    private String refreshType;

    @ApiModelProperty(value = "券活动id，必传", required = true, example = "2104090000000173")
    @NotNull
    private Long couponThemeId;

    @ApiModelProperty(value = "优惠券主键id", example = "10000001")
    private Long couponId;

}
