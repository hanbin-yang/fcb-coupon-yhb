package com.fcb.coupon.backend.service;

import java.util.List;


/**
 * 优惠券ES管理相关服务
 *
 * @Author WeiHaiQi
 * @Date 2021-06-18 15:50
 **/
public interface CouponEsManageService {

    /**
     * 刷新ES
     * @param couponThemeId     券活动id
     * @return 返回成功刷新数
     */
    long refreshEsByCouponThemeId(Long couponThemeId);

    /**
     * 优惠券全量同步
     */
    void syncAllCoupon();

    /**
     * 按券id批量刷新券
     * @param ids 券id列表
     */
    void refreshEsByCouponIds(List<Long> ids);

}
