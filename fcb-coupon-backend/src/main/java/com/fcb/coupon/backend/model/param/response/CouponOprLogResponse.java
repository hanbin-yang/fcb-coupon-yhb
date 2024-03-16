package com.fcb.coupon.backend.model.param.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 优惠券操作日志
 *
 * @Author WeiHaiQi
 * @Date 2021-06-23 17:38
 **/
@ApiModel(value="优惠券操作日志",description="优惠券操作日志")
@Data
public class CouponOprLogResponse implements Serializable {

    private static final long serialVersionUID = -5115174182037565294L;

    @ApiModelProperty(value = "操作类型的名称")
    private String oprSummary;

    @ApiModelProperty(value = "操作内容")
    private String operContent;

    @ApiModelProperty(value = "操作员的账户")
    private String createUsername;

    @ApiModelProperty(value = "操作时间")
    private Date createTime;
}
