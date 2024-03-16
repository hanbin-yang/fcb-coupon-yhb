package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.backend.model.param.request.FreezeCouponRequest;
import com.fcb.coupon.common.dto.UserInfo;
import lombok.Data;

/**
 * 冻结/解冻优惠券请求入参
 *
 * @Author WeiHaiQi
 * @Date 2021-06-22 17:03
 **/
@Data
public class FreezeCouponBo extends FreezeCouponRequest {

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
