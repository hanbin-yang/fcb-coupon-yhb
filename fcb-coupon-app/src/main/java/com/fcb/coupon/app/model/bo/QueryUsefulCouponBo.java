package com.fcb.coupon.app.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 名    称：QueryUsefulCouponBo
 * 功    能：
 * 创 建 人：mashiqiong
 * 创建时间：2021/08/24 10:56
 * 修 改 人：
 * 修改时间：
 * 说    明：
 * 版 本 号：
 */
@Data
@ApiModel(description = "查询可用券列表-订单-入参")
public class QueryUsefulCouponBo implements Serializable {
    private static final long serialVersionUID = 4149715848504993873L;

    @ApiModelProperty(value="明源项目id")
    private String itemId;

    @ApiModelProperty(value="物业类型 0住宅 1公寓 2商铺 3写字楼 4车位 5储藏室")
    private String propertyType;

    @ApiModelProperty(value="房源id")
    private String roomGuid;

    @ApiModelProperty(value="旧交易id", required = false)
    private String oldTransactionId;
    
    @ApiModelProperty(value="交易id", required = false)
    private String transactionId;

    @ApiModelProperty(value="手机号", required = true)
    private String phone;
}
