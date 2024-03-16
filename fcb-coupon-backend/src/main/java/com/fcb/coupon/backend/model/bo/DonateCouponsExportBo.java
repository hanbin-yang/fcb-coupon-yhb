package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.backend.model.param.request.DonateCouponsExportRequest;
import com.fcb.coupon.common.dto.UserInfo;
import lombok.Data;

/**
 * 赠送优惠券明细导出请求
 *
 * @Author WeiHaiQi
 * @Date 2021-06-25 17:17
 **/
@Data
public class DonateCouponsExportBo extends DonateCouponsExportRequest {

    /**
     * 当前登录用户id
     */
    private Long userId;

    public void loadUserInfo(UserInfo userInfo) {
        this.userId = userInfo.getId();
    }
}
