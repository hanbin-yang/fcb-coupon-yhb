package com.fcb.coupon.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fcb.coupon.app.exception.CouponGiveErrorCode;
import com.fcb.coupon.app.mapper.CouponBeforeGiveMapper;
import com.fcb.coupon.app.mapper.CouponGiveMapper;
import com.fcb.coupon.app.mapper.CouponMapper;
import com.fcb.coupon.app.mapper.CouponThemeMapper;
import com.fcb.coupon.app.model.dto.CouponBeforeGiveCacheDto;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.entity.CouponBeforeGiveEntity;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponGiveEntity;
import com.fcb.coupon.app.model.entity.CouponThemeEntity;
import com.fcb.coupon.app.service.CouponBeforeGiveCacheService;
import com.fcb.coupon.app.service.CouponThemeCacheService;
import com.fcb.coupon.common.constant.RedisCacheKeyConstant;
import com.fcb.coupon.common.enums.CouponDiscountType;
import com.fcb.coupon.common.enums.CouponEffDateCalType;
import com.fcb.coupon.common.enums.CouponRuleType;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author YangHanBin
 * @date 2021-08-13 10:06
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponBeforeGiveCacheServiceImpl implements CouponBeforeGiveCacheService {

    private final RedissonClient redissonClient;

    private final CouponBeforeGiveMapper couponBeforeGiveMapper;

    private final CouponMapper couponMapper;

    private final CouponThemeCacheService couponThemeCacheService;

    private final CouponGiveMapper couponGiveMapper;

    @Override
    public CouponBeforeGiveCacheDto getById(Long couponBeforeGiveId) {
        if (couponBeforeGiveId == null) {
            throw new BusinessException("3333", "劵赠送前记录id不能未空");
        }

        RMap<String, Object> rMap = getCouponBeforeGiveCacheRMap(couponBeforeGiveId);
        Map<String, Object> map = rMap.readAllMap();
        if (MapUtils.isEmpty(map)) {
            log.error("根据couponBeforeGiveId查询缓存不存在 couponBeforeGiveId={}", couponBeforeGiveId);
            return null;
        }
        return BeanUtil.fillBeanWithMap(map, new CouponBeforeGiveCacheDto(), true);
    }

    private RMap<String, Object> getCouponBeforeGiveCacheRMap(Long couponBeforeGiveId) {
        String cacheKey = RedisCacheKeyConstant.COUPON_BEFORE_GIVE + couponBeforeGiveId;
        return redissonClient.getMap(cacheKey, StringCodec.INSTANCE);
    }

    @Override
    public void refreshCouponBeforeGiveCache(Long couponBeforeGiveId) {

        if (couponBeforeGiveId == null) {
            log.error("刷新劵转赠缓存记录失败,转赠id为空[{}]",couponBeforeGiveId);
            throw new BusinessException(CouponGiveErrorCode.GIVE_COUPON_ID_NULL_ERROR);
        }
        CouponBeforeGiveEntity couponBeforeGiveEntity = couponBeforeGiveMapper.selectById(couponBeforeGiveId);

        if (Objects.isNull(couponBeforeGiveEntity)) {
            log.error("劵赠送前记录不存在,转赠id为空[{}]",couponBeforeGiveId);
            throw new BusinessException(CouponGiveErrorCode.GIVE_COUPON_NOT_FIND_ERROR);
        }
        //获取优惠劵信息
        CouponEntity couponEntity = couponMapper.selectById(couponBeforeGiveEntity.getCouponId());
        //从缓存获取劵活动信息
        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(couponEntity.getCouponThemeId());
        CouponGiveEntity couponGiveEntity = couponGiveMapper.selectById(couponBeforeGiveEntity.getCouponId());


        //组装缓存信息
        CouponBeforeGiveCacheDto reDto = new CouponBeforeGiveCacheDto();
        reDto.setId(couponBeforeGiveId);
        reDto.setCouponId(couponEntity.getId());
        reDto.setCouponStatus(couponEntity.getStatus());
        reDto.setReceiveUserId(couponGiveEntity == null ? null:Long.valueOf(couponGiveEntity.getReceiveUserId()));
        reDto.setExpireTime(couponBeforeGiveEntity.getExpireTime());
        reDto.setGiveNickname(couponBeforeGiveEntity.getGiveNickname());
        reDto.setGiveUserMobile(couponBeforeGiveEntity.getGiveUserMobile());
        reDto.setCouponName(couponThemeCache.getThemeTitle());
        reDto.setGiveAvatar(couponBeforeGiveEntity.getGiveAvatar());

        //计算优惠金额
        String discountAmount ;
        if (CouponDiscountType.DISCOUNT.getType().equals(couponThemeCache.getCouponDiscountType())) {
            discountAmount = couponThemeCache.getDiscountValue().toString().replaceAll("0+?$", "").replaceAll("[.]$", "");
            reDto.setDiscountType(CouponDiscountType.DISCOUNT.getType());
        } else {
            discountAmount = couponThemeCache.getDiscountAmount().toString();
            reDto.setDiscountType(CouponDiscountType.CASH.getType());
        }
        reDto.setDiscountAmount(discountAmount);
        //计算失效时间
        if (CouponEffDateCalType.FIXED.getType().equals(couponThemeCache.getEffDateCalcMethod())) {
            reDto.setEndTime(couponEntity.getEndTime());
        } else {
            Integer effDays = couponThemeCache.getEffDateDays();
            Date createTime = couponEntity.getCreateTime();
            DateTime endTime = DateUtil.offsetDay(createTime, effDays);
            reDto.setEndTime(endTime);
        }

        log.info("refreshCouponBeforeGiveCache即将刷新入缓存start: reDto=[{}]", JSON.toJSONString(reDto));
        RMap<String, Object> rMap = getCouponBeforeGiveCacheRMap(couponBeforeGiveId);
        Map<String, Object> map = BeanUtil.beanToMap(reDto, false, true);
        rMap.putAll(map);
        Date expireTime = couponBeforeGiveEntity.getExpireTime();

        long expireSeconds = 3600;
        if (expireTime.after(new Date())) {
            expireSeconds = RedisUtil.specifiedDateExpireSeconds(DateUtil.offset(expireTime, DateField.HOUR, 2));
        }

        rMap.expire(expireSeconds, TimeUnit.SECONDS);
        log.info("refreshCouponBeforeGiveCache刷新入缓存end: couponBeforeGiveId={},失效时间:[{}]", couponBeforeGiveId,expireSeconds);


    }
}
