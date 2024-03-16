package com.fcb.coupon.app.model.param.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 明源-优惠券信息Response
 *
 * @Author WeiHaiQi
 * @Date 2021-08-20 14:43
 **/
@Data
public class CouponMingyuanResponse implements Serializable {

    @ApiModelProperty(value = "券ID")
    private Long couponId;
    @ApiModelProperty(value = "手机",required = true)
    private String phone;
    @ApiModelProperty(value = "交易id")
    private String transactionId;
    @ApiModelProperty(value = "券金额、折扣数值统一*100")
    private BigDecimal couponValue;
    @ApiModelProperty(value = "券优惠类型 0 按金额 1 按折扣")
    private Integer couponDiscountType;
    @ApiModelProperty(value = "优惠券状态")
    private Integer status;
    @ApiModelProperty(value = "绑定的房间guid")
    private String roomGuid;
    @ApiModelProperty(value = "优惠券券码")
    private String couponCode;
}
