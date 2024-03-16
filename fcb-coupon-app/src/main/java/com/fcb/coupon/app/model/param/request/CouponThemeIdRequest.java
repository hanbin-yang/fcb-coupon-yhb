package com.fcb.coupon.app.model.param.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Author WeiHaiQi
 * @Date 2021-08-20 18:40
 **/
@Data
@ApiModel(value = "优惠券活动ID入参")
public class CouponThemeIdRequest implements Serializable {

    @ApiModelProperty(value = "券活动ID")
    @NotNull(message = "couponThemeId不能为空")
    private Long id;
}
