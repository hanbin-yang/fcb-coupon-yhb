package com.fcb.coupon.backend.business.couponSend.strategy;

import com.fcb.coupon.backend.business.couponSend.CouponSendStrategy;
import com.fcb.coupon.backend.model.dto.CouponMergedDto;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.service.CouponService;
import com.fcb.coupon.backend.service.CouponThemeCacheService;
import com.fcb.coupon.backend.service.CouponThemeService;
import com.fcb.coupon.common.constant.RedisCacheKeyConstant;
import com.fcb.coupon.common.enums.CouponEffDateCalType;
import com.fcb.coupon.common.enums.CouponStatusEnum;
import com.fcb.coupon.common.enums.CouponTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;
import com.fcb.coupon.common.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponSendThirdPartStrategy extends AbstractCouponSendStrategy {

    private final StringRedisTemplate stringRedisTemplate;
    private final CouponService couponService;
    private final CouponThemeCacheService couponThemeCacheService;

    @Override
    public Boolean supports(Integer couponType) {
        return CouponTypeEnum.COUPON_TYPE_THIRD.getType().equals(couponType);
    }

    @Override
    public void batchSend(List<CouponSendContext> couponSendContexts, CouponThemeEntity couponTheme) {
        List<CouponMergedDto> couponMergedDtos = new ArrayList<>(couponSendContexts.size());
        //券的redis队列key
        String listKey = RedisCacheKeyConstant.COUPON_THIRD_COUPON_LIST + couponTheme.getId();
        boolean canNotSend = false;
        try {
            for (CouponSendContext sendContext : couponSendContexts) {
                //券发完
                if (canNotSend) {
                    sendContext.error(true, "券已领完");
                    continue;
                }
                //从redis的list取出couponid
                String couponIdStr = stringRedisTemplate.opsForList().leftPop(listKey);
                if (StringUtils.isEmpty(couponIdStr)) {
                    //无可以领取券
                    canNotSend = true;
                    sendContext.error(true, "券已领完");
                    continue;
                }

                CouponMergedDto couponMergedDto = generateMergedCoupon(couponTheme, sendContext, Long.valueOf(couponIdStr));
                couponMergedDtos.add(couponMergedDto);
            }

            if (CollectionUtils.isEmpty(couponMergedDtos)) {
                return;
            }
            doBatchSend(couponMergedDtos);
            setFinishSendContexts(couponSendContexts, couponMergedDtos);
        } catch (BusinessException ex) {
            couponSendContexts.forEach(m -> {
                m.error(false, ex.getMessage());
            });
        } catch (Exception ex) {
            log.error("批量发券异常", ex);
            couponSendContexts.forEach(m -> {
                m.error(true, "发券异常");
            });
        }
    }

    private void doBatchSend(List<CouponMergedDto> couponMergedDtos) {
        if (CollectionUtils.isEmpty(couponMergedDtos)) {
            return;
        }
        CouponEntity couponEntity = couponMergedDtos.get(0).getCouponEntity();
        if (couponEntity == null) {
            return;
        }

        Long themeId = couponEntity.getCouponThemeId();

        //数据库保存券
        try {
            couponService.batchSendThirdPartCoupon(couponMergedDtos);
        } catch (Exception ex) {
            log.error("数据库批量发送第三方券异常", ex);
            //回滚券队列，可以考虑本地消息表来保证数据一致性，后面可优化
            rollbackCouponToRedis(themeId, couponMergedDtos);
            throw ex;
        }

        //同步redis库存
        try {
            couponThemeCacheService.deductStock(themeId, couponMergedDtos.size());
        } catch (Exception ex) {
            //操作异常，可以忽略，最终是通过redis的第三方券码队列和数据库乐观锁来保证库存数量
            log.error("批量发券同步redis库存异常", ex);
        }
    }

    private void rollbackCouponToRedis(Long themeId, List<CouponMergedDto> couponMergedDtos) {
        String listKey = RedisCacheKeyConstant.COUPON_THIRD_COUPON_LIST + themeId;
        List<String> couponIds = couponMergedDtos.stream().map(m -> m.getCouponEntity().getId().toString()).collect(Collectors.toList());
        stringRedisTemplate.opsForList().rightPushAll(listKey, couponIds);
    }

}
