package com.fcb.coupon.backend.model.param.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月05日 16:14:00
 */
@ApiModel(value = "优惠券获取列表异步导出结果", description = "优惠券获取列表异步导出结果")
@Data
public class CouponThemeExportAsyncResponse implements Serializable {

    @ApiModelProperty(value = "导出记录ID")
    private Long generateBatchId;
}
