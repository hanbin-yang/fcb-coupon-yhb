package com.fcb.coupon.backend.service;

/**
 * @author YangHanBin
 * @date 2021-07-29 10:11
 */
public interface CouponBeforeGiveCacheService {
    void refreshCouponBeforeGiveCache(Long couponBeforeGiveId);
}
