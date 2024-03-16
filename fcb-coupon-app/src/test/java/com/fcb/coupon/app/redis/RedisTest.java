package com.fcb.coupon.app.redis;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.BaseTest;
import com.fcb.coupon.app.model.dto.*;
import com.fcb.coupon.app.model.query.LambdaFieldNameSelector;
import com.fcb.coupon.app.service.CouponThemeCacheService;
import com.fcb.coupon.app.service.CouponUserStatisticCacheService;
import com.fcb.coupon.common.util.RedisUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.Date;
import java.util.Queue;

/**
 * @author YangHanBin
 * @date 2021-06-17 11:37
 */
public class RedisTest extends BaseTest {
    @Autowired
    private CouponUserStatisticCacheService couponUserStatisticCacheService;
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
    public void couponThemeCacheTest() {
        Long couponThemeId = 2108110000000044L;
        Integer result = couponThemeCacheService.deductStock(couponThemeId, 1);
    }

    @Test
    public void redisCacheTest() {
        Long couponThemeId = 2108110000000044L;
        String userId = "1411998190944296962";
        Integer userType = 2;
        CouponUserStatisticCache cacheDto = couponUserStatisticCacheService.getByUnionKey(couponThemeId, userId, userType);
        System.out.println("cacheDto = " + JSON.toJSONString(cacheDto));

        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(couponThemeId);

        PersonalReceiveDo incrReceiveCountDto = new PersonalReceiveDo();
        incrReceiveCountDto.setReceiveCount(1);
        incrReceiveCountDto.setCouponThemeId(couponThemeId);
        incrReceiveCountDto.setUserId(userId);
        incrReceiveCountDto.setUserType(userType);
        incrReceiveCountDto.setTotalLimit(couponThemeCache.getIndividualLimit());
        incrReceiveCountDto.setMonthLimit(couponThemeCache.getEveryMonthLimit());
        incrReceiveCountDto.setDayLimit(couponThemeCache.getEveryDayLimit());
        incrReceiveCountDto.setReceiveTime(new Date());
        UserReceiveMemento userReceiveMemento = couponUserStatisticCacheService.deductStock(incrReceiveCountDto);

        couponUserStatisticCacheService.rollbackStock(userReceiveMemento);
    }

    @Test
    public void couponThemeGetByIdTest() {
        Long couponThemeId = 2108110000000044L;
        LambdaFieldNameSelector<CouponThemeCache> selector = new LambdaFieldNameSelector<CouponThemeCache>()
                .select(CouponThemeCache::getCouponType)
                .select(CouponThemeCache::getActivityName)
                .select(CouponThemeCache::getCouponGiveRule);
        System.out.println("fFieldNames = " + selector.getFieldNames());
        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(couponThemeId, selector);
        System.out.println("couponThemeCache = " + JSON.toJSONString(couponThemeCache));

    }
}
