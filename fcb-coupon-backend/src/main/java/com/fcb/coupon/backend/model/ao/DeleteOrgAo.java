package com.fcb.coupon.backend.model.ao;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 17:52
 */
@Data
@ApiModel(description = "删除商家 入参Ao")
public class DeleteOrgAo {
    @ApiModelProperty(value = "mkt_use_rule表的主键id")
    @JsonProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "组织名称")
    @JsonProperty(value = "name")
    private String orgName;
}
