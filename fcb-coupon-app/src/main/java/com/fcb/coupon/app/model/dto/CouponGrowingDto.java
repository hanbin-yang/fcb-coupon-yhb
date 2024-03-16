package com.fcb.coupon.app.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author mashiqiong
 * @date 2021-8-16 10:47
 */
@Data
public class CouponGrowingDto {
    /**
     * 券活动ID
     */
    private Long couponThemeId;

    /**
     * 券ID
     */
    private Long couponId;

    /**
     * 券活动名称
     */
    private String themeTitle;

    /**
     * 用户unionId
     */
    private String unionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 发券时间
     */
    private Date bindTime;

    /**
     * 核销时间
     */
    private Date usedTime;

    /**
     * 楼盘名称
     */
    private String usedStoreName;

    /**
     * 楼盘ID
     */
    private Long usedStoreId;

    /**
     * 用户类型,0是会员,1是机构经纪人,2是C端用户
     */
    private Integer userType;
}
