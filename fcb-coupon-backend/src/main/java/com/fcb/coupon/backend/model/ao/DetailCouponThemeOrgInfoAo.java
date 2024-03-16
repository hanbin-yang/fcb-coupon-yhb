package com.fcb.coupon.backend.model.ao;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mashiqiong
 * @date 2021-6-23 17:26
 */
@Data
public class DetailCouponThemeOrgInfoAo implements Serializable {
    private static final long serialVersionUID = -4113453289121482375L;
    /**
     * 组织id
     */
    @ApiModelProperty(value = "组织id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long orgId;

    /**
     * 组织名称
     */
    @ApiModelProperty(value = "组织名称")
    private String orgName;

    /**
     * 组织代码
     */
    @ApiModelProperty(value = "组织级别Code")
    private String orgLevelCode;
}
