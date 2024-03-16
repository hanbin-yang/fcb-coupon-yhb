package com.fcb.coupon.backend.model.param.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author YangHanBin
 * @date 2021-07-29 9:59
 */
@Data
@ApiModel("刷新转增前记录 入参")
public class RefreshCouponBeforeGiveCacheRequest {
    @ApiModelProperty(value = "coupon_before_give表主键id")
    private Long couponBeforeGiveId;
}
