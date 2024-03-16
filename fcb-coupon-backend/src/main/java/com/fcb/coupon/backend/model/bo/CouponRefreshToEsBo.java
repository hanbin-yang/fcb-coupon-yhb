package com.fcb.coupon.backend.model.bo;

import lombok.Data;

import java.util.Date;

/**
 *
 * @Author WeiHaiQi
 * @Date 2021-07-14 17:13
 **/
@Data
public class CouponRefreshToEsBo {

    /**
     * 刷新类型标志，通过券活动id全量刷：refreshByCouponThemeId 手机号和用户类型刷：refreshByUserPhoneAndUserType 券id刷：refreshByCouponId
     */
    private String refreshType;

    /**
     * 券活动id
     */
    private Long couponThemeId;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 用户类型 0会员(B端)用户 1机构用户 2C端用户
     */
    private Integer userType;

    /**
     * 优惠券主键id
     */
    private Long couponId;

    /**
     * 支持时间范围刷新 coupon表中的createTime字段 开始时间
     */
    private Date couponCreateTimeStart;

    /**
     * 支持时间范围刷新 coupon表中的createTime字段 结束时间
     */
    private Date couponCreateTimeEnd;
}
