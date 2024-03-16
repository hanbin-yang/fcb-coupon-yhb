package com.fcb.coupon.backend.business.couponSend.strategy;

import com.fcb.coupon.backend.business.couponSend.CouponSendStrategy;
import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.model.dto.CouponMergedDto;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.*;
import com.fcb.coupon.backend.service.CouponService;
import com.fcb.coupon.backend.service.CouponThemeCacheService;
import com.fcb.coupon.backend.service.CouponThemeService;
import com.fcb.coupon.backend.service.CouponThemeStatisticService;
import com.fcb.coupon.common.enums.*;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.*;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.collect.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;

/*
发券使用自动生成方式
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponSendGenerateStrategy extends AbstractCouponSendStrategy {

    private final CouponService couponService;

    private final CouponThemeStatisticService couponThemeStatisticService;

    private final CouponThemeCacheService couponThemeCacheService;

    @Override
    public Boolean supports(Integer couponType) {
        return !CouponTypeEnum.COUPON_TYPE_THIRD.getType().equals(couponType);
    }

    @Override
    public void batchSend(List<CouponSendContext> couponSendContexts, CouponThemeEntity couponTheme) {
        if (CollectionUtils.isEmpty(couponSendContexts)) {
            return;
        }
        //生成券信息，用户的券信息
        List<CouponMergedDto> couponMergedDtos = generateMergedCoupons(couponTheme, couponSendContexts);
        try {
            //批量生产券
            doBatchSend(couponMergedDtos);
            setFinishSendContexts(couponSendContexts, couponMergedDtos);
        } catch (BusinessException ex) {
            //库存不足，降级库存数量发券
            if (CouponThemeErrorCode.COUPON_THEME_CAN_SEND_COUNT_ERROR.getCode().equalsIgnoreCase(ex.getCode())) {
                fallbackBatchSend(couponMergedDtos, couponSendContexts);
            } else {
                couponSendContexts.forEach(m -> {
                    m.error(false, ex.getMessage());
                });
            }
        } catch (Exception ex) {
            log.error("批量发券异常", ex);
            couponSendContexts.forEach(m -> {
                m.error(true, "发券异常");
            });
        }
    }


    /*
    降级发券，有可能是总数操过发券数量
     */
    private void fallbackBatchSend(List<CouponMergedDto> couponMergedDtos, List<CouponSendContext> couponSendContexts) {
        List<CouponMergedDto> canCouponMergedDtos = null;
        try {
            //从数据库查询库存数据
            CouponThemeStatisticEntity statisticEntity = couponThemeStatisticService.getById(couponSendContexts.get(0).getCouponThemeId());
            if (statisticEntity == null || statisticEntity.getCreatedCount() <= statisticEntity.getSendedCount()) {
                couponSendContexts.forEach(m -> {
                    m.error(false, "券已领完");
                });
                return;
            }
            int canSendCount = statisticEntity.getCreatedCount() - statisticEntity.getSendedCount();
            List<CouponSendContext> canSendContexts = couponSendContexts.subList(0, canSendCount);
            canCouponMergedDtos = couponMergedDtos.subList(0, canSendCount);
            doBatchSend(canCouponMergedDtos);
            setFinishSendContexts(canSendContexts, canCouponMergedDtos);
        } catch (BusinessException ex) {
            //库存可能还是不足，降级单个发送
            if (CouponThemeErrorCode.COUPON_THEME_CAN_SEND_COUNT_ERROR.getCode().equalsIgnoreCase(ex.getCode()) && !CollectionUtils.isEmpty(canCouponMergedDtos)) {
                fallbackSingleSend(canCouponMergedDtos, couponSendContexts);
            } else {
                couponSendContexts.forEach(m -> {
                    m.error(false, ex.getMessage());
                });
            }
        } catch (Exception ex) {
            log.error("降级批量发券异常", ex);
            couponSendContexts.forEach(m -> {
                m.error(true, "发券异常");
            });
        }
    }

    /*
    降级单个发券，有可能是总数操过发券数量
     */
    private List<CouponSendContext> fallbackSingleSend(List<CouponMergedDto> couponMergedDtos, List<CouponSendContext> couponSendContexts) {
        for (int i = 0; i < couponSendContexts.size(); i++) {
            CouponSendContext couponSendContext = couponSendContexts.get(i);
            CouponMergedDto couponMergedDto = couponMergedDtos.get(i);

            try {
                doBatchSend(Lists.newArrayList(couponMergedDto));
                couponSendContext.success(couponMergedDto.getCouponEntity(), couponMergedDto.getCouponUserEntity());
            } catch (BusinessException ex) {
                couponSendContext.error(false, ex.getMessage());
            } catch (Exception ex) {
                log.error("降级单个发券异常", ex);
                couponSendContext.error(true, "发券异常");
            }
        }
        return couponSendContexts;
    }


    private List<CouponMergedDto> generateMergedCoupons(CouponThemeEntity couponTheme, List<CouponSendContext> couponSendContexts) {
        List<CouponMergedDto> couponMergedDtos = new ArrayList<>(couponSendContexts.size());
        Queue<Long> idList = RedisUtil.generateIds(couponSendContexts.size());
        for (CouponSendContext sendContext : couponSendContexts) {
            CouponMergedDto couponMergedDto = generateMergedCoupon(couponTheme, sendContext, idList.poll());
            couponMergedDtos.add(couponMergedDto);
        }
        return couponMergedDtos;
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
        couponService.batchSendGenerateCoupon(couponMergedDtos);

        //同步redis库存
        try {
            couponThemeCacheService.deductStock(themeId, couponMergedDtos.size());
        } catch (Exception ex) {
            //操作异常，可以忽略，最终是通过数据库乐观锁来保证库存数量
            log.error("批量发券同步redis库存异常", ex);
        }
    }

}
