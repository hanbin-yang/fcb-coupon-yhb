package com.fcb.coupon.app.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * @author YangHanBin
 * @date 2021-06-18 11:03
 */
public enum OscPageInfoErrorCode implements ResponseErrorCode {
    LOAD_FAIL("223001", "加载配置项失败"),
            ;

    private final String code;

    private final String message;

    OscPageInfoErrorCode(String code, String message) {
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
