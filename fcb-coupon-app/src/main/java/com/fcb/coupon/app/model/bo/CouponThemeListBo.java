package com.fcb.coupon.app.model.bo;

import lombok.Data;

import java.util.List;

/**
 * 微吼查询优惠券活动列表（后台）
 *
 * @author mashiqiong
 * @date 2021-8-17 15:30
 */
@Data
public class CouponThemeListBo {

    private String userId;

    private Integer userType;

    /**
     * 优惠券Ids
     */
    private List<Long> ids;

}
