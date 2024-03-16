package com.fcb.coupon.app.model.param.response;

import lombok.Data;
import java.io.Serializable;

/**
 * 优惠券分类
 *
 * @Author WeiHaiQi
 * @Date 2021-08-18 19:06
 **/
@Data
public class CouponDiscountTypeResponse implements Serializable {
    private Integer code;
    private String value;
}
