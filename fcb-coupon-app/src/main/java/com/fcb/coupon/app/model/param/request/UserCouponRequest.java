package com.fcb.coupon.app.model.param.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 查询用户优惠券信息请求参数
 *
 * @Author WeiHaiQi
 * @Date 2021-08-23 16:37
 **/
@Data
public class UserCouponRequest implements Serializable {

    @ApiModelProperty("unionId")
    private String unionId;
}
