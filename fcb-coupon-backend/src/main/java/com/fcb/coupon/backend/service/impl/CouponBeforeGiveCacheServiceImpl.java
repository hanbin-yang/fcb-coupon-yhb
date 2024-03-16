package com.fcb.coupon.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.exception.CouponBeforeGiveErrorCode;
import com.fcb.coupon.backend.mapper.CouponBeforeGiveMapper;
import com.fcb.coupon.backend.service.CouponBeforeGiveCacheService;
import com.fcb.coupon.backend.service.CouponGiveService;
import com.fcb.coupon.backend.service.CouponService;
import com.fcb.coupon.backend.service.CouponThemeService;
import com.fcb.coupon.common.constant.RedisCacheKeyConstant;
import com.fcb.coupon.common.enums.CouponDiscountType;
import com.fcb.coupon.common.enums.CouponEffDateCalType;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.RedisUtil;
import com.fcb.coupon.backend.model.cache.CouponBeforeGiveCache;
import com.fcb.coupon.backend.model.entity.CouponBeforeGiveEntity;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponGiveEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author YangHanBin
 * @date 2021-07-29 10:12
 */
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class CouponBeforeGiveCacheServiceImpl extends ServiceImpl<CouponBeforeGiveMapper, CouponBeforeGiveEntity> implements CouponBeforeGiveCacheService {
    private final CouponGiveService couponGiveService;
    private final CouponThemeService couponThemeService;
    private final CouponService couponService;
    private final RedissonClient redissonClient;

    @Override
    public void refreshCouponBeforeGiveCache(Long couponBeforeGiveId) {
        if (couponBeforeGiveId == null) {
            throw new BusinessException(CouponBeforeGiveErrorCode.ID_NULL);
        }

        CouponBeforeGiveEntity couponBeforeGiveEntity = baseMapper.selectById(couponBeforeGiveId);
        if (Objects.isNull(couponBeforeGiveEntity)) {
            throw new BusinessException(CouponBeforeGiveErrorCode.RECORD_NOT_EXIST);
        }

        CouponEntity couponEntity = couponService.getBaseMapper().selectById(couponBeforeGiveEntity.getCouponId());

        CouponGiveEntity couponGiveEntity = couponGiveService.getBaseMapper().selectById(couponBeforeGiveEntity.getCouponId());

        CouponThemeEntity couponThemeEntity = couponThemeService.getBaseMapper().selectById(couponBeforeGiveEntity.getCouponThemeId());

        CouponBeforeGiveCache cacheBean = prepareCouponBeforeGiveBean(couponBeforeGiveEntity, couponEntity, couponGiveEntity, couponThemeEntity);

        log.info("refreshCouponBeforeGiveCache即将刷新入缓存start: reDto={}", JSON.toJSONString(cacheBean));
        RMap<String, Object> rMap = getCouponBeforeGiveCacheRMap(couponBeforeGiveId);
        Map<String, Object> map = BeanUtil.beanToMap(cacheBean, false, true);
        rMap.putAll(map);
        Date expireTime = couponBeforeGiveEntity.getExpireTime();

        long expireSeconds = 3600;
        if (expireTime.after(new Date())) {
            expireSeconds = RedisUtil.specifiedDateExpireSeconds(DateUtil.offset(expireTime, DateField.HOUR, 2));
        }

        rMap.expire(expireSeconds, TimeUnit.SECONDS);
        log.info("refreshCouponBeforeGiveCache刷新入缓存end: couponBeforeGiveId={}", couponBeforeGiveId);
    }

    private CouponBeforeGiveCache prepareCouponBeforeGiveBean(CouponBeforeGiveEntity couponBeforeGiveEntity, CouponEntity couponEntity, CouponGiveEntity couponGiveEntity, CouponThemeEntity couponThemeEntity) {
        CouponBeforeGiveCache cacheBean = new CouponBeforeGiveCache();
        cacheBean.setId(couponBeforeGiveEntity.getId());
        cacheBean.setCouponId(couponGiveEntity.getCouponId());
        cacheBean.setCouponStatus(couponEntity.getStatus());
        cacheBean.setReceiveUserId(couponGiveEntity.getReceiveUserId());
        cacheBean.setExpireTime(couponBeforeGiveEntity.getExpireTime());
        cacheBean.setGiveNickname(couponBeforeGiveEntity.getGiveNickname());
        cacheBean.setGiveUserMobile(couponBeforeGiveEntity.getGiveUserMobile());

        cacheBean.setCouponName(couponThemeEntity.getThemeTitle());
        cacheBean.setGiveAvatar(couponBeforeGiveEntity.getGiveAvatar());

        StringBuilder discountOrAmount = new StringBuilder();
        if (CouponDiscountType.DISCOUNT.getType().equals(couponThemeEntity.getCouponDiscountType())) {
            BigDecimal bigDecimal = new BigDecimal(couponThemeEntity.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            String ruleAmount = bigDecimal.toString().replaceAll("0+?$", "").replaceAll("[.]$", "");
            discountOrAmount.append(ruleAmount);
            cacheBean.setDiscountType(CouponDiscountType.DISCOUNT.getType());
        } else {
            int ruleAmount = couponThemeEntity.getDiscountAmount().intValue();
            discountOrAmount.append(ruleAmount);
            cacheBean.setDiscountType(CouponDiscountType.CASH.getType());
        }
        cacheBean.setDiscountAmount(discountOrAmount.toString());

        if (CouponEffDateCalType.FIXED.getType().equals(couponThemeEntity.getEffDateCalcMethod())) {
            cacheBean.setEndTime(couponEntity.getEndTime());
        } else {
            Integer effDays = couponThemeEntity.getEffDateDays();
            Date createTime = couponEntity.getCreateTime();
            DateTime endTime = DateUtil.offsetDay(createTime, effDays);
            cacheBean.setEndTime(endTime);
        }
        return cacheBean;
    }

    private RMap<String, Object> getCouponBeforeGiveCacheRMap(Long couponBeforeGiveId) {
        String cacheKey = RedisCacheKeyConstant.COUPON_BEFORE_GIVE + couponBeforeGiveId;
        return redissonClient.getMap(cacheKey, StringCodec.INSTANCE);
    }
}
