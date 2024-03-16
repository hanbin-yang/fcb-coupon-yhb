package com.fcb.coupon.backend.model.bo;

import lombok.Data;

import java.util.List;

/**
 * 营销中心->查询券活动的统计信息 入参
 * @author mashiqiong
 * @date 2021-07-29 20:59
 *
 */
@Data
public class CouponThemeStatisticsBo {

    /**
     * 优惠券Ids
     */
    private List<Long> couponThemeIdList;
}
