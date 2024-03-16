package com.fcb.coupon.app.remote.dto.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author YangHanBin
 * @date 2021-08-24 14:09
 */
@Data
@ApiModel(value = "校验促销活动是否可叠加优惠券请求参数", description = "校验促销活动是否可叠加优惠券请求参数")
public class PromotionCheckResInput {
    private static final long serialVersionUID = 6441192856757660480L;
    @ApiModelProperty(value = "房源ID", required = true)
    @NotBlank(message = "房源ID不能为空")
    private String roomGuid;
}
