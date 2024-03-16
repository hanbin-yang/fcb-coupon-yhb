package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.backend.model.param.request.PostponeCouponRequest;
import com.fcb.coupon.common.dto.UserInfo;
import lombok.Data;

/**
 * 优惠券延期请求
 *
 * @Author WeiHaiQi
 * @Date 2021-06-23 8:40
 **/
@Data
public class PostponeCouponBo extends PostponeCouponRequest {

    /**
     * 当前登录用户id
     */
    private Long userId;
    /**
     * 当前登录用户名
     */
    private String username;

    public void loadUserInfo(UserInfo userInfo) {
        this.userId = userInfo.getId();
        this.username = userInfo.getUsername();
    }
}
