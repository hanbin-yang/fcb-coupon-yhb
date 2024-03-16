package com.fcb.coupon.backend.model.bo;

import lombok.Data;

import java.util.Date;

/**
 * @author YangHanBin
 * @date 2021-06-17 16:42
 */
@Data
public class CouponThemeUpdateAfterCheckBo {
    /**
     * 当前登录用户id
     */
    private Long userId;
    /**
     * 当前登录用户名
     */
    private String username;
    /**
     * 当前登录用户组织级别
     */
    private String userOrgLevelCode;

    /**
     * 券活动ID
     */
    private Long couponThemeId;

    /**
     * 券活动结束时间
     */
    private Date endTime;
    /**
     * 券活动使用说明
     */
    private String themeDesc;
    /**
     * 券码有效期 结束时间
     */
    private Date endTimeConfig;
}
