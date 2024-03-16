package com.fcb.coupon.common.enums;

import java.util.Objects;

public enum CouponThemeFrontStatusEnum {

	IMMEDIATELY_GET(1, "立即领取"),
	GET_AGAIN(2, "再次领取"),
	FINISHED(3, "已结束"),
	HAS_GONE(4, "已抢光"),
	NOT_STARTED(5, "未开始"),
	ALREADY_RECEIVED(6, "已领取"),
	;

	private Integer status;
	private String statusStr;

	public Integer getStatus() {
		return status;
	}

	public String getStatusStr() {
		return statusStr;
	}

	CouponThemeFrontStatusEnum(Integer status, String statusStr) {
		this.status = status;
		this.statusStr = statusStr;
	}
	
	public static String getFrontStatusStr(Integer frontStatus) {
		if (Objects.isNull(frontStatus)) {
			return null;
		}
		for (CouponThemeFrontStatusEnum status : CouponThemeFrontStatusEnum.values()) {
			if (Objects.equals(status.getStatus(), frontStatus)) {
				return status.statusStr;
			}
		}
		return null;
	}

}
