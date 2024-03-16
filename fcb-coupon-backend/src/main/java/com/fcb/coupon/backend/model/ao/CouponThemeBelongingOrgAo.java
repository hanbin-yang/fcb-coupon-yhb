package com.fcb.coupon.backend.model.ao;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author YangHanBin
 * @date 2021-06-15 10:12
 */
@ApiModel(description = "券活动 费用归属 入参")
@Data
public class CouponThemeBelongingOrgAo implements Serializable {
    @ApiModelProperty(value = "组织id", required = true)
    private Long orgId;

    @ApiModelProperty(value = "组织名称", required = true)
    private String orgName;

    @ApiModelProperty(value = "组织级别Code", required = true)
    private String orgLevelCode;
}