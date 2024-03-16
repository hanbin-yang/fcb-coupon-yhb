package com.fcb.coupon.app.remote.dto.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author YangHanBin
 * @date 2021-08-24 14:10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "校验促销活动是否可叠加优惠券响应对象", description = "校验促销活动是否可叠加优惠券响应对象")
public class PromotionCheckResOutput {
    private static final long serialVersionUID = 5838918906787109669L;

    @ApiModelProperty("活动ID")
    private String actId;

    @ApiModelProperty("活动名称")
    private String name;

    @ApiModelProperty("优惠标签")
    private String label;

    @ApiModelProperty("详情页描述")
    private String desc;

    @ApiModelProperty("促销类型 1 直降 2 折扣 3 特价")
    private Integer promotionType;

    @ApiModelProperty("优惠值 1 直降(元) 2 折扣（折) 3 特价")
    private String promotionValue;

    @ApiModelProperty(value = "是否能叠加使用优惠券 0:不可使用，1:可使用")
    private Integer canUseCoupon;
}
