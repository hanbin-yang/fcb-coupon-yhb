package com.fcb.coupon.app.business.couponreceive.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fcb.coupon.app.business.couponreceive.CouponReceiveContext;
import com.fcb.coupon.app.business.couponreceive.processor.CouponReceivePostProcessor;
import com.fcb.coupon.app.business.couponreceive.strategy.CouponReceiveStrategy;
import com.fcb.coupon.app.exception.CouponReceiveErrorCode;
import com.fcb.coupon.app.infra.inteceptor.AppUserInfo;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.bo.CouponReceiveBo;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.service.CouponThemeCacheService;
import com.fcb.coupon.app.service.CouponUserStatisticCacheService;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.RStock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author YangHanBin
 * @date 2021-08-16 9:45
 */
@Slf4j
public abstract class AbstractCouponReceiveHandler implements CouponReceiveHandler {
    @Resource
    private CouponThemeCacheService couponThemeCacheService;

    @Resource
    private List<CouponReceivePostProcessor> couponReceivePostProcessors;

    @Resource
    private List<CouponReceiveStrategy> strategies;

    @Resource
    private CouponUserStatisticCacheService couponUserStatisticCacheService;

    @Override
    public void validate(CouponReceiveBo bo, CouponThemeCache couponTheme) {
        validateApplicableUserType(couponTheme);
    }

    @Override
    public CouponEntity handle(CouponReceiveBo bo, CouponThemeCache couponTheme) {
        // redis扣减库存
        Integer realCount = deductCouponThemeStock(couponTheme.getId(), bo.getReceiveCount());
        // 库存不足
        if (!Objects.equals(realCount, bo.getReceiveCount())) {
            log.error("handle#deductStock 券活动库存不足: couponThemeId={}, realCount={}, needCount={}", bo.getCouponThemeId(), realCount, bo.getReceiveCount());
            throw new BusinessException(CouponReceiveErrorCode.OUT_OF_STOCK);
        }

        CouponReceiveContext context = buildCouponReceiveContext(bo, couponTheme, realCount);

        return handlerInternal(context);
    }

    protected abstract AppUserInfo getUserInfoByMobile(String mobile);

    abstract void validateApplicableUserType(CouponThemeCache couponTheme);

    protected JSONArray getCouponThemeApplicableUserTypes(String applicableUserTypes) {
        JSONObject jsonObject = JSON.parseObject(applicableUserTypes);
        return jsonObject.getJSONArray("ids");
    }

    /**
     * redis减库存
     * @param couponThemeId couponThemeId
     */
    protected Integer deductCouponThemeStock(Long couponThemeId, int count) {
        // redis预减库存
        Integer realCount = couponThemeCacheService.deductStock(couponThemeId, count, RStock.DeductMode.NO);
        if (realCount == null) {
            throw new BusinessException(CouponReceiveErrorCode.COUPON_THEME_NOT_EXIST);
        }

        return realCount;
    }

    protected CouponReceiveContext buildCouponReceiveContext(CouponReceiveBo bo, CouponThemeCache couponTheme, Integer realCount) {
        if (StringUtils.isBlank(bo.getUserId())) {
            // 根据手机号获取userId
            try {
                AppUserInfo userInfo = getUserInfoByMobile(bo.getUserMobile());
                bo.setUserId(userInfo.getUserId());
            } catch (Exception e) {
                couponThemeCacheService.rollbackStock(bo.getCouponThemeId(), realCount);
                throw e;
            }
        }

        return CouponReceiveContext.builder()
                .couponThemeId(couponTheme.getId())
                .userType(bo.getUserType())
                .userId(bo.getUserId())
                .userMobile(bo.getUserMobile())
                .source(bo.getSource())
                .sourceId(bo.getSourceId())
                .couponThemeCache(couponTheme)
                .receiveTime(new Date())
                .receiveCount(realCount)
                .build();
    }

    private CouponReceivePostProcessor getPostProcessor(Integer source) {
        for (CouponReceivePostProcessor couponSendPostProcessor : couponReceivePostProcessors) {
            if (couponSendPostProcessor.supports(source)) {
                return couponSendPostProcessor;
            }
        }
        log.error("AbstractCouponReceiveHandler#getPostProcessor error: source={}", source);
        throw new BusinessException(CouponReceiveErrorCode.SOURCE_ILLEGAL);
    }

    private CouponReceiveStrategy getReceiveStrategy(Integer couponType) {
        for (CouponReceiveStrategy strategy : strategies) {
            if (strategy.supports(couponType)) {
                return strategy;
            }
        }
        log.error("AbstractCouponReceiveHandler#getReceiveStrategy error: couponType={}", couponType);
        throw new BusinessException(CouponReceiveErrorCode.COUPON_TYPE_ILLEGAL);
    }

    protected CouponEntity handlerInternal(CouponReceiveContext context) {
        CouponReceivePostProcessor receivePostProcessor = null;
        try {
            receivePostProcessor = getPostProcessor(context.getSource());
            receivePostProcessor.postProcessBeforeReceive(context);
        } catch (Exception e) {
            // 回滚redis预扣的库存
            log.error("before receive redis rollback start：couponThemeId={}, rollbackCount={}", context.getCouponThemeId(), context.getReceiveCount());
            try {
                couponThemeCacheService.rollbackStock(context.getCouponThemeId(), context.getReceiveCount());
            } catch (Exception exception) {
                log.error("before receive redis rollback fail：couponThemeId={}, rollbackCount={}", context.getCouponThemeId(), context.getReceiveCount(), e);
            }
            log.error("before receive redis rollback end：couponThemeId={}, rollbackCount={}", context.getCouponThemeId(), context.getReceiveCount());
            throw e;
        }

        CouponEntity couponEntity = null;
        try {
            CouponReceiveStrategy strategy = getReceiveStrategy(context.getCouponThemeCache().getCouponType());
            couponEntity = strategy.receive(context);
        } catch (Exception e) {
            // 回滚redis预扣的库存
            log.error("after receive redis rollback start：couponThemeId={}, rollbackCount={}", context.getCouponThemeId(), context.getReceiveCount());
            try {
                couponThemeCacheService.rollbackStock(context.getCouponThemeId(), context.getReceiveCount());
                couponUserStatisticCacheService.rollbackStock(context.getUserReceiveMemento());
            } catch (Exception exception) {
                log.error("after receive redis rollback fail：couponThemeId={}, rollbackCount={}", context.getCouponThemeId(), context.getReceiveCount(), e);
            }
            log.error("after receive redis rollback end：couponThemeId={}, rollbackCount={}", context.getCouponThemeId(), context.getReceiveCount());
            throw e;
        }

        receivePostProcessor.postProcessAfterReceive(context);

        return couponEntity;
    }

}
