package com.fcb.coupon.backend;

import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.query.LambdaFieldNameSelector;

/**
 * @author HanBin_Yang
 * @since 2021/6/24 10:23
 */
public class CommonMainTest {
    public static void main(String[] args) {
        LambdaFieldNameSelector<CouponThemeCache> selectWrapper = new LambdaFieldNameSelector<>(CouponThemeCache.class)
                .select(CouponThemeCache::getCouponType)
                .select(CouponThemeCache::getActivityName)
                .select(CouponThemeCache::getCouponGiveRule);
        System.out.println("fieldNames = " + selectWrapper.getFieldNames());
    }
}
