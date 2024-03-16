package com.fcb.coupon.backend.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * 优惠券操作异常码
 *
 * @Author WeiHaiQi
 * @Date 2021-06-19 9:59
 **/
public enum CouponErrorCode implements ResponseErrorCode {

    UPDATE_COUPON_INVALID_USED_NOT_ALLOWED("333001", "已使用的优惠券不能作废."),
    UPDATE_COUPON_INVALID_EXCEPTION("333002", "作废优惠券异常."),
    UPDATE_COUPON_FREEZE_EXCEPTION("333003", "冻结/解冻优惠券异常."),
    UPDATE_COUPON_FREEZE_STATUS_NOT_ALLOWED("333004", "非已发行/可使用的优惠券不能冻结."),
    UPDATE_COUPON_POSTPONE_USED_NOT_ALLOWED("333006", "已使用的优惠券不能延期."),
    UPDATE_COUPON_POSTPONE_EXCEPTION("333007", "延期优惠券异常."),
    REFRESH_ES_COUPON_EXCEPTION("333008", "延期优惠券异常."),
    ;

    private final String code;
    private final String message;

    CouponErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
