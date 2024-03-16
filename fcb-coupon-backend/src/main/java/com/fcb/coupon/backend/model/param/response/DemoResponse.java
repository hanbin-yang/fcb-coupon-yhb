package com.fcb.coupon.backend.model.param.response;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月09日 20:03:00
 */
@Data
public class DemoResponse implements Serializable {


    @JSONField(name = "ID")
    @ApiModelProperty(value = "券id")
    private Long id;
    @ApiModelProperty(value = "券码")
    private String couponCode;
}
