package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.common.dto.UserInfo;
import lombok.Data;

/**
 * 后台管理->营销中心->优惠券管理->券核销->券核销列表
 * @author mashiqiong
 * @date 2021-6-28 21:23
 */
@Data
public class AsyncTaskListBo {

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

    /**
     *
     * @description <pre>
     * 根据页码和每页记录数获取页起始记录
     * </pre>
     * @return
     */
    public int getStartItem() {

        int start = (currentPage - 1) * itemsPerPage;
        if (start < 0) {
            start = 0;
        }
        return start;
    }
}
