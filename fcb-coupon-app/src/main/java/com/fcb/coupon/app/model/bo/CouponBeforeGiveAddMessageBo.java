package com.fcb.coupon.app.model.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponBeforeGiveAddMessageBo {
	private String receiveUserMobile;
	private String giveUserName;
	private String giveUserMobile; 
	private Integer couponDiscountType; 
	private BigDecimal couponValue;
	private String url;
}
