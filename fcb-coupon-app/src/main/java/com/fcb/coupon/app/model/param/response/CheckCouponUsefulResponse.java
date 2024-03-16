package com.fcb.coupon.app.model.param.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author mashiqiong
 * @date 2021-08-25 17:42
 */
@Data
@ApiModel(description = "明源订单-操作优惠券 出参")
public class CheckCouponUsefulResponse implements Serializable {
    @ApiModelProperty(value = "不能操作券的状态码code")
    private String errorCode;

    @ApiModelProperty(value = "不能操作券的原因message")
    private String errorMessage;

    @ApiModelProperty(value = "券主键")
    private Long couponId;

    @ApiModelProperty(value = "优惠券名称")
    private String couponName;

    @ApiModelProperty("优惠券状态 1.可使用 2已使用 12已上锁")
    private Integer status;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CheckCouponUsefulResponse that = (CheckCouponUsefulResponse) o;
        return Objects.equals(couponId, that.couponId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(couponId);
    }
}
