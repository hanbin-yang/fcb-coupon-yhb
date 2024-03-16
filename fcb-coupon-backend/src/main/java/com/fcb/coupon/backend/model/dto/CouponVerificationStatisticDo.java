package com.fcb.coupon.backend.model.dto;

/**
 * 统一已核销数量
 * @author YangHanBin
 * @date 2021-09-08 9:32
 */
public class CouponVerificationStatisticDo {
    /**
     * couponThemeId
     */
    private Long couponThemeId;
    /**
     * 统计数量
     */
    private int count;


    public Long getCouponThemeId() {
        return couponThemeId;
    }

    public void setCouponThemeId(Long couponThemeId) {
        this.couponThemeId = couponThemeId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
