package com.fcb.coupon.app.model.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author YangHanBin
 * @date 2021-08-17 14:24
 */
@Data
@Builder
public class CouponUserInfo {
    /**
     * 客户id
     */
    private String userId;
    /**
     * 统一用户ID
     */
    private String unionId;
    /**
     * 昵称
     */
    private String userName;
    /**
     * 手机号
     */
    private String userMobile;
}
