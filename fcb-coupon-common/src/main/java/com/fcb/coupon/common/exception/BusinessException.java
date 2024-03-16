package com.fcb.coupon.common.exception;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月09日 19:32:00
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = -6123730651531475735L;

    private String code;

    private String message;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResponseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
