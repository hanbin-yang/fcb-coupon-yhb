package com.fcb.coupon.backend.model.bo;

import lombok.Data;



/**
 * @author YangHanBin
 * @date 2021-06-18 12:00
 */
@Data
public class GenerateCouponBo {
    /**
     * 登录用户信息
     */
    private Long userId;
    private String username;

    /**
     * 券活动id
     */
    private Long couponThemeId;
    /**
     * 券来源
     */
    private Integer source;
    /**
     * 生券数量
     */
    private Integer generateAmount;
}
