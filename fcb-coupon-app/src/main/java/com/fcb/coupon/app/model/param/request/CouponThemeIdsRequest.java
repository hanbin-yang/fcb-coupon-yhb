package com.fcb.coupon.app.model.param.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年09月01日 18:14:00
 */
@Data
@ApiModel(value = "优惠券活动ID列表入参")
public class CouponThemeIdsRequest implements Serializable {

    @ApiModelProperty(value = "券活动ID列表")
    @NotEmpty(message = "券活动ID列表不能为空")
    @Size(min = 1, message = "券活动ID列表不能为空")
    private List<Long> ids;
}
