package com.fcb.coupon.app.model.param.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月26日 11:00:00
 */
@Data
@ApiModel(value = "优惠券转赠领取参数")
public class CouponGiveReceiveRequest implements Serializable {

    @NotBlank(message = "转赠记录编码不能为空")
    @ApiModelProperty(value = "转赠记录编码")
    private String beforeGiveCode;

}
