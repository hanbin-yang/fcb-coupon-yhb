package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.backend.model.param.request.CouponOprLogQueryRequest;
import com.fcb.coupon.common.dto.UserInfo;
import lombok.Data;

/**
 * 查询优惠券操作日志请求参数
 *
 * @Author WeiHaiQi
 * @Date 2021-06-23 17:07
 **/
@Data
public class CouponOprLogQueryBo extends CouponOprLogQueryRequest {

    /**
     * 当前登录用户id
     */
    private Long userId;

    public void loadUserInfo(UserInfo userInfo) {
        this.userId = userInfo.getId();
    }

    public int getStartItem() {
        int currentPage = this.getCurrentPage();
        int itemsPerPage = this.getItemsPerPage();
        currentPage = currentPage == 0 ? 1 : this.getCurrentPage();
        int start = (currentPage - 1) * itemsPerPage;
        if (start < 0) {
            start = 0;
        }
        return start;
    }
}
