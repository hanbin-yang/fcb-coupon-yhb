package com.fcb.coupon.app.model.param.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 楼盘-查询优惠券活动列表及当前用户领券情况
 *
 * @Author WeiHaiQi
 * @Date 2021-08-23 18:03
 **/
@Data
public class CouponThemeListHouseRequest implements Serializable {

    @ApiModelProperty(value = "券活动ID", dataType = "List<Long>")
    @NotEmpty
    private List<Long> ids;

    @ApiModelProperty(value = "unionid", dataType = "String")
    private String userId;

}
