package com.fcb.coupon.backend.model.param.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月16日 19:27:00
 */
@Data
public class CouponSendUserRequest implements Serializable {

    @ApiModelProperty(value = "手机号", required = false)
    private String telPhone;

    @ApiModelProperty(value = "用户id", required = true)
    private String userId;

    @ApiModelProperty(value = "统一用户中心", required = true)
    private String unionId;

    @ApiModelProperty(value = "发送券的唯一ID", required = true)
    private String mongoId;

}
