package com.fcb.coupon.client.backend.param.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CouponThemeCrmResponse implements Serializable {

    @ApiModelProperty(value = "优惠券id")
    private Long id;

    @ApiModelProperty(value = "优惠券名称")
    private String themeTitle;

    @ApiModelProperty(value = "是否可赠送")
    private Integer canDonation;

    @ApiModelProperty(value = "是否可转让")
    private Integer canAssign;

    /**
     * 使用人群
     **/
    private List<Integer> crowdIds;//使用人群，格式：{"ids":[0,1]} 0会员 1机构经济人 2C端用户
}
