package com.fcb.coupon.app.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author YangHanBin
 * @date 2021-08-24 9:58
 */
@Data
@ApiModel("需要操作的优惠券信息 入参")
public class OperateCouponDto implements Serializable {
    @ApiModelProperty(value = "优惠券主键id")
    private Long couponId;
}
