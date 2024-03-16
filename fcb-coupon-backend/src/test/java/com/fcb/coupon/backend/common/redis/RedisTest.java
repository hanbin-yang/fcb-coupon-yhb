package com.fcb.coupon.backend.common.redis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fcb.coupon.BaseTest;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.query.LambdaFieldNameSelector;
import com.fcb.coupon.backend.service.CouponThemeCacheService;
import com.fcb.coupon.common.util.RedisUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Queue;

/**
 * @author YangHanBin
 * @date 2021-06-17 11:37
 */
public class RedisTest extends BaseTest {
    @Autowired
    private CouponThemeCacheService couponThemeCacheService;
    @Test
    public void generateIdsTest() {
        Queue<Long> ids = RedisUtil.generateIds(3);
        System.out.println("ids = " + ids);
        Long id1 = ids.poll();
        System.out.println("id1 = " + id1);
        Long id2 = ids.poll();
        System.out.println("id2 = " + id2);
        Long id3 = ids.poll();
        System.out.println("id3 = " + id3);
    }

    @Test
    public void redisCacheTest() {
        Long couponThemeId = 2108110000000044L;
        Integer realCount = couponThemeCacheService.rollbackStock(couponThemeId, 3);
        System.out.println("realCount = " + realCount);


        LambdaFieldNameSelector<CouponThemeCache> selector = new LambdaFieldNameSelector<>(CouponThemeCache.class)
                .select(CouponThemeCache::getCouponType)
                .select(CouponThemeCache::getActivityName)
                .select(CouponThemeCache::getCouponGiveRule);
        CouponThemeCache selectedFieldsById = couponThemeCacheService.getById(couponThemeId, selector);
        System.out.println("selectedFieldsById = " + selectedFieldsById);
        LambdaQueryWrapper<CouponThemeCache> couponThemeCacheLambdaQueryWrapper = new LambdaQueryWrapper<>(CouponThemeCache.class);
        LambdaQueryWrapper<CouponThemeCache> select = couponThemeCacheLambdaQueryWrapper.select(CouponThemeCache::getCouponType);

    }
}
