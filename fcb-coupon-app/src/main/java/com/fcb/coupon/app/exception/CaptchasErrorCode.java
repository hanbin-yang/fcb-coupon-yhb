package com.fcb.coupon.app.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * @author mashiqiong
 * @date 2021-8-19 11:23
 */
public enum CaptchasErrorCode implements ResponseErrorCode {
    CAPTCHAS_UN_SUPPORTED_BUSINESS("270001", "不支持的验证码类型"),
    CAPTCHAS_OBTAIN_CODE_AGAIN("270002", "请重新获取验证码"),
    CAPTCHAS_DISABLED("270003", "验证码校验失败"),
    CAPTCHAS_ERROR_LOGIN_INFORMATION("270004", "获取登录人信息出错!"),
    CAPTCHAS_MOBILE_CANNOT_BE_EMPTY("270005", "手机号码不能为空!"),
    CAPTCHAS_MOBILE_INCONSISTENT("270006", "输入的手机号码与登录人的手机号不一致!"),
    CAPTCHAS_SEND_AT_MOST_ONE_PER_SECOND("270007", "每%s秒最多发送一条验证码"),
    ;
    private final String code;

    private final String message;

    CaptchasErrorCode(String code, String message) {
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
