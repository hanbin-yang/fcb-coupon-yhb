package com.fcb.coupon.common.dto;

import com.fcb.coupon.common.constant.CouponConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月11日 15:00:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private T data;
    private String code;
    private String message;

    public static <T> Result<T> success(String msg) {
        return success(null, CouponConstant.SUCCESS_CODE, msg);
    }

    public static <T> Result<T> success(T model, String msg) {
        return success(model, CouponConstant.SUCCESS_CODE, msg);
    }

    public static <T> Result<T> success(T model) {
        return success(model, CouponConstant.SUCCESS_CODE, "");
    }

    public static <T> Result<T> success(T datas, String code, String msg) {
        return new Result<>(datas, code, msg);
    }

    public static <T> Result<T> error(String code, String msg, T datas) {
        return new Result<>(datas, code, msg);
    }

    public static <T> Result<T> error(String code, String msg) {
        return error(code, msg, null);
    }
}
