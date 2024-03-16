package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.backend.model.param.request.CouponGenerateBatchRequest;

import com.fcb.coupon.common.dto.UserInfo;
import lombok.Data;

@Data
public class CouponGenerateBatchBo extends CouponGenerateBatchRequest {

    /**
     * 当前登录用户id
     */
    private Long userId;

    public void loadUserInfo(UserInfo userInfo) {
        this.userId = userInfo.getId();
    }

}
