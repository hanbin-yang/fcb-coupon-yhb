package com.fcb.coupon.app.model.param.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年09月02日 11:45:00
 */
@Data
public class ThemeIdRequest implements Serializable {

    @ApiModelProperty(value = "券活动ID")
    @NotNull(message = "优惠券活动Id不能为空")
    private Long couponThemeId;
}
