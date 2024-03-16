package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.backend.model.param.request.CouponExportRequest;
import com.fcb.coupon.common.dto.UserInfo;
import lombok.Data;

/**
 * 优惠券明细导出请求
 *
 * @Author WeiHaiQi
 * @Date 2021-06-24 15:18
 **/
@Data
public class CouponExportBo extends CouponExportRequest {

    /**
     * 当前登录用户id
     */
    private Long userId;

    public void loadUserInfo(UserInfo userInfo) {
        this.userId = userInfo.getId();
    }
}
