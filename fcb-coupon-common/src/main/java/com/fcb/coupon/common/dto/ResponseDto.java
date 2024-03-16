package com.fcb.coupon.common.dto;

import com.fcb.coupon.common.constant.CouponConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 接口同一返回实体定义 ResponseDto
 *
 * @param <T>
 */
@Data
@ApiModel("通用响应体")
public class ResponseDto<T> implements Serializable {

    @ApiModelProperty(value = "错误码", required = true, extensions = @Extension(name = "since", properties = {@ExtensionProperty(name = "1.0", value = "add")}))
    private String code = CouponConstant.SUCCESS_CODE;
    @ApiModelProperty(value = "错误信息", extensions = @Extension(name = "since", properties = {@ExtensionProperty(name = "1.0", value = "add")}))
    private String message;
    @ApiModelProperty(value = "响应结果", extensions = @Extension(name = "since", properties = {@ExtensionProperty(name = "1.0", value = "add")}))
    private T data;


    public ResponseDto() {

    }

    public ResponseDto(String message) {
        this.code = CouponConstant.SUCCESS_CODE;
        this.message = message;
    }

    public ResponseDto(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseDto(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResponseDto(String code, T data) {
        this.code = code;
        this.data = data;
    }

}

