package com.fcb.coupon.backend.model.param.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author YangHanBin
 * @date 2021-07-29 16:05
 */
@Data
@ApiModel(description = "单个核销券详情")
public class CouponVerificationDetailResponse {
    @ApiModelProperty(value = "券码")
    private String couponCode;

    @ApiModelProperty(value = "使用券的明源认购书编号")
    @JsonProperty(value = "orderCode")
    private String subscribeCode;

    @ApiModelProperty(value = "用户类型")
    @JsonProperty(value = "crowdScopeId")
    private Integer userType;

    @ApiModelProperty(value = "绑定手机号")
    private String bindTel;

    @ApiModelProperty(value = "核销店铺名称")
    private String usedStoreName;

    @ApiModelProperty(value = "核销店铺编码")
    @JsonProperty(value = "buildCode")
    private String usedStoreCode;
}
