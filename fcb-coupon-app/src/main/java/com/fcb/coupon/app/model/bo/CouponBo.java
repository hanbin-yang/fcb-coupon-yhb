package com.fcb.coupon.app.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 券详情查询
 *
 * @Author WeiHaiQi
 * @Date 2021-08-17 18:13
 **/
@Data
public class CouponBo implements Serializable {

    /**
     * 券id
     */
    private Long couponId;
    /**
     * unionId
     */
    private String unionId;
    /**
     * 用户ID
     */
    private String realUserId;
    /**
     * 用户类型
     */
    private Integer userType;
}
