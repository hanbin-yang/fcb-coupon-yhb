package com.fcb.coupon.backend.model.ao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 10:08
 */
@Data
@ApiModel(description = "添加组织 入参Ao")
public class AddOrgAo implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty(value = "id")
    @ApiModelProperty(value = "组织id", required = true)
    private Long orgId;

    @JsonProperty(value = "name")
    @ApiModelProperty(value = "组织名称")
    private String orgName;

    @JsonProperty(value = "regionName")
    @ApiModelProperty(value = "组织代码，店铺时为楼盘编码")
    private String orgCode;
}
