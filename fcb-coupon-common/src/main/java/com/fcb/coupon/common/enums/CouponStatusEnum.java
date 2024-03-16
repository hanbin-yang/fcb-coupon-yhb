package com.fcb.coupon.common.enums;

/**
 * 优惠券状态
 * @author weihaiqi
 * @date 2021-06-16
 */
public enum CouponStatusEnum {

	STATUS_MIDDLE(-999, "中间态"),
	STATUS_ALL(-1, "所有"),

	STATUS_ISSUE(0, "已发行"),
	STATUS_USE(1, "可使用"),
	STATUS_USED(2, "已使用"),
	STATUS_CANCEL(3, "已作废"),
	STATUS_INVALID(4, "已失效"),
	STATUS_DONATE(5, "已赠送"),
	STATUS_BEFORE_DONATE(51, "转赠中"),
	STATUS_ASSIGN(10, "已转让"),
	STATUS_FREEZE(11, "已冻结"),
	STATUS_LOCKED(12,"已上锁"),
	;

	private Integer status;
	private String statusStr;

	public Integer getStatus() {
		return status;
	}

	public String getStatusStr() {
		return statusStr;
	}

	CouponStatusEnum(Integer status, String statusStr) {
		this.status = status;
		this.statusStr = statusStr;
	}

	/**
	 * 取中文描述
	 * @param status
	 * @return
	 */
	public static String getStrByStatus(Integer status) {
		if (status != null) {
			for (CouponStatusEnum s : CouponStatusEnum.values()) {
				if (s.getStatus().equals(status)) {
					return s.statusStr;
				}
			}
		}
		return null;
	}
}
