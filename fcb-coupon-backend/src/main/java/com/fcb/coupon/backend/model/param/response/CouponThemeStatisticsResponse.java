package com.fcb.coupon.backend.model.param.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 营销中心->查询券活动的统计信息 出参
 * @author mashiqiong
 * @date 2021-07-29 20:59
 */
@ApiModel(value="查询券活动的统计信息",description="查询券活动的统计信息")
@Data
public class CouponThemeStatisticsResponse implements Serializable {
    private static final long serialVersionUID = -1698868954971347739L;

    @ApiModelProperty(value = "优惠券ID")
    private Long id;
    @ApiModelProperty(value = "优惠券类型")
    private Integer couponType;
    @ApiModelProperty(value = "券活动限制总张数")
    private Integer totalLimit;
    @ApiModelProperty(value = "已经生成的张数")
    private Integer drawedCoupons;
    @ApiModelProperty(value = "已领取的张数")
    private Integer sendedCouopns;
    @ApiModelProperty(value = "已使用的张数")
    private Integer usedCouopns;
    @ApiModelProperty(value = "可发行的张数")
    private Integer canSendCoupons;
}
