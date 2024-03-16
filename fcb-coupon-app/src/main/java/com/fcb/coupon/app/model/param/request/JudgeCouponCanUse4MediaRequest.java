package com.fcb.coupon.app.model.param.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author YangHanBin
 * @date 2021-08-20 8:55
 */
@ApiModel(description = "媒体广告-判断couponThemeId是否可用--入参")
@Data
public class JudgeCouponCanUse4MediaRequest {
    @ApiModelProperty(value = "券活动id")
    @NotNull(message = "themeId不能为空")
    @JsonProperty(value = "themeId")
    private Long couponThemeId;
}
