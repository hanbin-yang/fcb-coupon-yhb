package com.fcb.coupon.app.model.param.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 主动营销批次发放优惠券
 *
 * @Author WeiHaiQi
 * @Date 2021-08-13 10:27
 **/
@Data
public class MarketingCouponUseCountResponse implements Serializable {

    private static final long serialVersionUID = 5001060017194265988L;

    @ApiModelProperty(value = "批次号（taskId）")
    private String batchNo;
    @ApiModelProperty(value = "优惠券发送成功数量")
    private Long couponSuccessCount;
    @ApiModelProperty(value = "实发人数")
    private Long userCount;
}
