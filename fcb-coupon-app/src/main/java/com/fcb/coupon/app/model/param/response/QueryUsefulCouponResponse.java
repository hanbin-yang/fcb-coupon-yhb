package com.fcb.coupon.app.model.param.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@ApiModel(description = "查询可用券列表-订单 出参")
@Data
public class QueryUsefulCouponResponse implements Serializable {
	private static final long serialVersionUID = 5689862717043826889L;
	@ApiModelProperty("优惠券主键id")
	private String couponId;

	@ApiModelProperty("优惠券名称")
	private String couponName;

	@ApiModelProperty("券金额、折扣")
	private Long couponValue;

	@ApiModelProperty("券优惠类型 0 按金额 1 按折扣")
	private Integer couponDiscountType;

	@ApiModelProperty("优惠券生效时间")
	@JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date startTime;

	@ApiModelProperty("优惠券最迟使用时间")
	@JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date endTime;

	@ApiModelProperty("优惠券状态 1.可使用 2已使用 12已上锁")
	private Integer status;

	@ApiModelProperty(value = "绑定的房间id")
	private String roomGuid;
	
	@ApiModelProperty(value = "绑定的房间名称")
	private String roomName;
	
	@ApiModelProperty(value = "使用限制  0：无限制， 其他值：订单最小金额限制")
	private BigDecimal useLimit;
	
	@ApiModelProperty(value = "单个订单使用该类型券张数限制")
	private Integer orderUseLimit;
	
	@ApiModelProperty(value="原交易id")
    private String transactionId;

	@ApiModelProperty(value = "优惠券券码")
	private String couponCode;
}
