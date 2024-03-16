package com.fcb.coupon.backend.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * @author YangHanBin
 * @date 2021-07-29 10:15
 */
public enum CouponBeforeGiveErrorCode implements ResponseErrorCode {
    ID_NULL("240001", "劵赠送前记录id不能未空"),
    RECORD_NOT_EXIST("240002", "劵赠送前记录不存在"),
    ;

    private final String code;

    private final String message;

    CouponBeforeGiveErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }
}
