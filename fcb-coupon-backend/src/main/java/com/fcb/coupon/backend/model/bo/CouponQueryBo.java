package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.backend.model.param.request.CouponQueryRequest;
import com.fcb.coupon.common.dto.UserInfo;
import lombok.Data;

import java.util.List;

/**
 * TODO
 *
 * @Author WeiHaiQi
 * @Date 2021-06-22 16:38
 **/
@Data
public class CouponQueryBo extends CouponQueryRequest {

    /**
     * 楼盘id集合
     */
    private List<Long> storeIds;
    /**
     * 商家id
     */
    private List<Long> merchantIds;
    /**
     * 集团id
     */
    private List<Long> groupIds;
    /**
     * 当前登录用户id
     */
    private Long userId;
    /**
     * 当前页码
     */
    private Integer currentPage;

    /**
     * 页面pageSize
     */
    private Integer itemsPerPage;

    public void loadUserInfo(UserInfo userInfo) {
        this.userId = userInfo.getId();
    }
}
