package com.fcb.coupon.backend.model.param.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 营销中心->查询券活动的统计信息 入参
 *
 * @author mashiqiong
 * @date 2021-07-29 20:32
 */
@ApiModel(description = "查询券活动的统计信息-入参")
@Data
public class CouponThemeStatisticsRequest implements Serializable {

    @ApiModelProperty(value = "券活动IDs", dataType = "List<Long>")
    private List<Long> couponThemeIdList;

}
