package com.fcb.coupon.client.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月24日 09:42:00
 */
@Data
@ApiModel("通用响应体")
public class RestResult<T> implements Serializable {

    public static String SUCCESS = "0";

    /*
    兼容老接口调用
     */
    @ApiModelProperty(value = "标识")
    private boolean flag;

    @ApiModelProperty(value = "错误码", required = true)
    private String code = SUCCESS;
    @ApiModelProperty(value = "错误信息")
    private String message;
    @ApiModelProperty(value = "响应结果")
    private T data;


    public RestResult(String message) {
        this.flag = true;
        this.code = SUCCESS;
        this.message = message;
    }

    public RestResult(T data) {
        this.flag = true;
        this.code = SUCCESS;
        this.data = data;
    }

    public RestResult(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public RestResult(String code, String message, T data) {
        this.flag = SUCCESS.equalsIgnoreCase(code);
        this.code = code;
        this.message = message;
        this.data = data;
    }


    public RestResult(String code, T data) {
        this.flag = SUCCESS.equalsIgnoreCase(code);
        this.code = code;
        this.data = data;
    }

}
