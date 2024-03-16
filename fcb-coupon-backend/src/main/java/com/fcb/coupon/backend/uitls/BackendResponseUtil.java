package com.fcb.coupon.backend.uitls;

import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * 接口返回实体工具类
 */
public class BackendResponseUtil {
    private static final String BACKEND_SUCCESS_CODE = "0";

    public static <T> ResponseDto<T> success(){
        return successMessage(CouponConstant.SUCCESS_MESSAGE);
    }

    public static <T> ResponseDto<T> successMessage(String message){
        return success(message, null);
    }

    public static <T> ResponseDto<T> successObj(T data){
        return success(CouponConstant.SUCCESS_MESSAGE, data);
    }

    public static <T> ResponseDto<T> success(String message, T data){
        return new ResponseDto<>(BACKEND_SUCCESS_CODE, message, data);
    }

    public static <T> ResponseDto<T> fail(ResponseErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> ResponseDto<T> fail(ResponseErrorCode errorCode, T data) {
        return newResult(errorCode.getCode(), errorCode.getMessage(), data);
    }

    public static <T> ResponseDto<T> fail(String code, String message) {
        return new ResponseDto<>(code, message, null);
    }

    public static <T> ResponseDto<T> newResult(String code, String message, T data) {
        return new ResponseDto<>(code, message, data);
    }
}
