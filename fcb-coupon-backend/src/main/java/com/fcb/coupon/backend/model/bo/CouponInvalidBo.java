package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.common.dto.UserInfo;
import lombok.Data;

import java.util.List;

/**
 * 作废优惠券明细Bo
 *
 * @Author WeiHaiQi
 * @Date 2021-06-19 8:12
 **/
@Data
public class CouponInvalidBo {

    /**
     * 优惠券id
     */
    private List<Long> idList;
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
