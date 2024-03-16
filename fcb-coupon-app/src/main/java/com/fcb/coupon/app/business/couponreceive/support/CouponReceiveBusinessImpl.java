package com.fcb.coupon.app.business.couponreceive.support;

import com.fcb.coupon.app.business.couponreceive.CouponReceiveBusiness;
import com.fcb.coupon.app.business.couponreceive.handler.CouponReceiveHandler;
import com.fcb.coupon.app.exception.CouponReceiveErrorCode;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.bo.CouponReceiveBo;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.service.CouponThemeCacheService;
import com.fcb.coupon.common.enums.CouponThemeStatus;
import com.fcb.coupon.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author YangHanBin
 * @date 2021-08-16 8:38
 */
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class CouponReceiveBusinessImpl implements CouponReceiveBusiness {
    @Resource
    private CouponThemeCacheService couponThemeCacheService;
    @Resource
    private List<CouponReceiveHandler> receiveHandlers;

    @Override
    public CouponEntity receive(CouponReceiveBo bo, CouponThemeCache couponThemeCache) {
        if (Objects.isNull(couponThemeCache)) {
            couponThemeCache = getCouponThemeCache(bo.getCouponThemeId());
        }

        if (Objects.isNull(couponThemeCache)) {
            throw new BusinessException(CouponReceiveErrorCode.COUPON_THEME_NOT_EXIST);
        }
        // 券活动相关校验
        this.validate(bo, couponThemeCache);
        CouponReceiveHandler handler = getHandler(bo.getUserType());
        // 用户类型相关校验
        handler.validate(bo, couponThemeCache);

        return handler.handle(bo, couponThemeCache);
    }

    protected void validateCouponTheme(CouponThemeCache couponThemeCache) {
        // 校验状态
        if (!CouponThemeStatus.EFFECTIVE.getStatus().equals(couponThemeCache.getStatus())) {
            throw new BusinessException(CouponReceiveErrorCode.COUPON_THEME_NOT_EFFECTIVE);
        }
        // 校验有效期
        Date nowTime = new Date();
        if (nowTime.before(couponThemeCache.getStartTime())) {
            throw new BusinessException(CouponReceiveErrorCode.COUPON_THEME_NOT_START);
        } else if (nowTime.after(couponThemeCache.getEndTime())) {
            throw new BusinessException(CouponReceiveErrorCode.COUPON_THEME_ENDED);
        }
    }

    protected void validate(CouponReceiveBo bo, CouponThemeCache couponTheme) {
        validateCouponTheme(couponTheme);
    }

    private CouponReceiveHandler getHandler(Integer userType) {
        for (CouponReceiveHandler receiveHandler : receiveHandlers) {
            if (receiveHandler.supports(userType)) {
                return receiveHandler;
            }
        }
        log.error("CouponReceiveBusinessImpl#getHandler error: userType={}", userType);
        throw new BusinessException(CouponReceiveErrorCode.USER_TYPE_ILLEGAL);
    }

    @Override
    public CouponThemeCache getCouponThemeCache(Long couponThemeId) {
        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(couponThemeId);
        if (Objects.isNull(couponThemeCache)) {
            throw new BusinessException(CouponReceiveErrorCode.COUPON_THEME_NOT_EXIST);
        }
        return couponThemeCache;
    }
}
