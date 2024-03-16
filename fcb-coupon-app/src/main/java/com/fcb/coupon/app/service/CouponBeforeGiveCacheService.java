package com.fcb.coupon.app.service;

import com.fcb.coupon.app.model.dto.CouponBeforeGiveCacheDto;

/**
 * @author YangHanBin
 * @date 2021-08-13 10:02
 */
public interface CouponBeforeGiveCacheService {

    /**
     * 根据券转赠id去缓存获取劵转赠记录信息
     * @param couponBeforeGiveId
     * @return
     */
    CouponBeforeGiveCacheDto getById(Long couponBeforeGiveId);

    /**
     * 刷新劵转赠记录信息
     * @param couponBeforeGiveId
     * @throws Exception
     */
    void refreshCouponBeforeGiveCache(Long couponBeforeGiveId);
}
