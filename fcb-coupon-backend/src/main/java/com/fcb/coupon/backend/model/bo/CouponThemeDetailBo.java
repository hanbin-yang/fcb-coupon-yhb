package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.common.dto.UserInfo;
import lombok.Data;

/**
 * 营销中心->优惠券管理->查看优惠券活动详情 入参
 * @author mashiqiong
 * @date 2021-06-23 10:43
 */
@Data
public class CouponThemeDetailBo {

    /**
     * 优惠券Id
     */
    private Long id;

    /**
     * 当前登录用户id
     */
    private Long userId;

    /**
     * 当前登录用户组织级别
     */
    private String userOrgLevelCode;

    public void loadUserInfo(UserInfo userInfo) {
        this.userId = userInfo.getId();
        this.userOrgLevelCode = userInfo.getOrgLevelCode();
    }
}
