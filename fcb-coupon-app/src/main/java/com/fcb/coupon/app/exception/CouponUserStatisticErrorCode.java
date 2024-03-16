package com.fcb.coupon.app.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * @author YangHanBin
 * @date 2021-08-16 16:09
 */
public enum CouponUserStatisticErrorCode implements ResponseErrorCode {
    OUT_OF_INDIVIDUAL_LIMIT("260000", "总领取已达上限"),
    OUT_OF_MONTH_LIMIT("260001", "本月领取已达上限"),
    OUT_OF_DAY_LIMIT("260002", "今天领取已达上限"),
    ;

    private final String code;

    private final String message;

    CouponUserStatisticErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
