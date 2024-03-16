package com.fcb.coupon.backend.model.param.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author YangHanBin
 * @date 2021-06-17 15:11
 */
@Data
@ApiModel(description = "提交审核--入参")
public class CouponThemeSubmitAuditRequest implements Serializable {
    private static final long serialVersionUID = -4850349549675168205L;

    @ApiModelProperty(value = "券活动id", required = true, dataType = "Long")
    @JsonProperty(value = "id")
    @NotNull(message = "券活动id不能为空")
    private Long couponThemeId;
}
