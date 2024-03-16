package com.fcb.coupon.app.model.param.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by mashiqiong on 7/9/21.
 */
@ApiModel(description = "统计转赠前的优惠券信息次数（短信赠送） 入参")
@Data
public class CouponBeforeGiveGetSmsCountRequest  implements Serializable {
	private static final long serialVersionUID = 2959600300068900871L;

	@ApiModelProperty(value = "券id")
	private Long couponId;

}
