package com.fcb.coupon.app.business.couponreceive.strategy;

import com.fcb.coupon.app.business.couponreceive.CouponReceiveContext;
import com.fcb.coupon.app.exception.CouponThemeErrorCode;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponUserEntity;
import com.fcb.coupon.app.model.entity.CouponUserStatisticEntity;
import com.fcb.coupon.app.service.CouponService;
import com.fcb.coupon.common.constant.RedisCacheKeyConstant;
import com.fcb.coupon.common.enums.CouponTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * 第三方券码领券
 *
 * @author YangHanBin
 * @date 2021-08-16 14:04
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class ThirdCouponCodeReceiveStrategy extends AbstractCouponReceiveStrategy {
    private final StringRedisTemplate stringRedisTemplate;
    private final CouponService couponService;

    @Override
    public boolean supports(Integer couponType) {
        return CouponTypeEnum.COUPON_TYPE_THIRD.getType().equals(couponType);
    }

    @Override
    public CouponEntity receive(CouponReceiveContext context) {
        Long couponId = deductCouponIdFromCache(context.getCouponThemeId());
        Tuple3<CouponEntity, CouponUserEntity, CouponUserStatisticEntity> tuple3 = prepareCouponReceiveRelatedEntityBean(context, couponId);
        doReceive(tuple3.getT1(), tuple3.getT2(), tuple3.getT3());
        context.setCouponEntity(tuple3.getT1());
        return tuple3.getT1();
    }

    private void doReceive(CouponEntity couponEntity, CouponUserEntity couponUserEntity, CouponUserStatisticEntity couponUserStatisticEntity) {
        try {
            couponService.receiveCouponWithTx(couponEntity, couponUserEntity, couponUserStatisticEntity);
        } catch (Exception e) {
            // 回滚redis库存
            log.error("doReceive rollbackCouponIdToCache start：couponThemeId={}, rollbackCount={}, couponId={}", couponEntity.getCouponThemeId(), couponUserStatisticEntity.getReceiveCount(), couponEntity.getId());
            try {
                // 回滚couponId库存
                rollbackCouponIdToCache(couponEntity.getCouponThemeId(), couponEntity.getId());
            } catch (Exception exception) {
                log.error("doReceive rollbackCouponIdToCache fail：couponThemeId={}, rollbackCount={}, couponId={}", couponEntity.getCouponThemeId(), couponUserStatisticEntity.getReceiveCount(), couponEntity.getId(), e);
            }
            log.error("doReceive rollbackCouponIdToCache end：couponThemeId={}, rollbackCount={}, couponId={}", couponEntity.getCouponThemeId(), couponUserStatisticEntity.getReceiveCount(), couponEntity.getId());
            throw e;
        }
    }

    private Long deductCouponIdFromCache(Long couponThemeId) {
        //券的redis队列key
        String cacheKey = RedisCacheKeyConstant.COUPON_THIRD_COUPON_LIST + couponThemeId;
        //从redis的list取出couponId
        String couponIdStr = stringRedisTemplate.opsForList().leftPop(cacheKey);
        if (StringUtils.isEmpty(couponIdStr)) {
            log.error("ThirdCouponCodeReceiveStrategy#stringRedisTemplate.opsForList() 券活动库存不足: cacheKey={}", cacheKey);
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_CAN_SEND_COUNT_ERROR);
        }
        return Long.parseLong(couponIdStr);
    }

    private void rollbackCouponIdToCache(Long couponThemeId, Long couponId) {
        //券的redis队列key
        String cacheKey = RedisCacheKeyConstant.COUPON_THIRD_COUPON_LIST + couponThemeId;
        stringRedisTemplate.opsForList().rightPush(cacheKey, String.valueOf(couponId));
    }


    private Tuple3<CouponEntity, CouponUserEntity, CouponUserStatisticEntity> prepareCouponReceiveRelatedEntityBean(CouponReceiveContext context, Long couponId) {
        CouponThemeCache couponThemeCache = context.getCouponThemeCache();
        CouponEntity couponEntity = getCouponEntity(context, couponId, couponThemeCache);

        CouponUserEntity couponUserEntity = prepareCouponUserEntity(context, couponEntity);

        CouponUserStatisticEntity couponUserStatisticEntity = prepareCouponUserStatisticBean(context);

        return Tuples.of(couponEntity, couponUserEntity, couponUserStatisticEntity);
    }
}
