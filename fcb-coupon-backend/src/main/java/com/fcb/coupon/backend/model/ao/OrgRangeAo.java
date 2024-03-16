package com.fcb.coupon.backend.model.ao;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mashiqiong
 * @date 2021-6-18 15:06
 */
@Data
@ApiModel(description = "发布范围入参")
public class OrgRangeAo  implements Serializable {
    @ApiModelProperty(value = "组织id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;
    @ApiModelProperty(value = "所属级别")
    private Integer level;

    /**
     * 组织代码
     */
    private String orgCode;
    /**
     * 父级组织代码
     */
    private String parentCode;
}
