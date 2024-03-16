package com.fcb.coupon.backend.model.param.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author YangHanBin
 * @date 2021-06-17 15:30
 */
@Data
@ApiModel(description = "审核通过--入参")
public class CouponThemeAuditPassRequest implements Serializable {
    private static final long serialVersionUID = -1089094121632873720L;

    @ApiModelProperty(value = "券活动ID", required = true, dataType = "Long")
    @JsonProperty(value = "id")
    @NotNull(message = "券活动id不能为空")
    private Long couponThemeId;

    @ApiModelProperty(value = "审核描述", required = true, dataType = "String")
    @NotBlank(message = "审核描述不能为空")
    private String remark;
}
