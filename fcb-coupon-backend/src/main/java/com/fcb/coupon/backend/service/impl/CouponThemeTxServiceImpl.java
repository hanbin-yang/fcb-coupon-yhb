package com.fcb.coupon.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeOrgEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeStatisticEntity;
import com.fcb.coupon.backend.model.query.LambdaFieldNameSelector;
import com.fcb.coupon.backend.service.*;
import com.fcb.coupon.common.enums.CouponThemeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.function.Tuple2;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
@Service
public class CouponThemeTxServiceImpl implements CouponThemeTxService {

    private final CouponThemeService couponThemeService;
    private final CouponThemeStatisticService couponThemeStatisticService;
    private final CouponThemeOrgService couponThemeOrgService;
    private final CouponService couponService;

    private final CouponThemeCacheService couponThemeCacheService;


    /**
     * 事务控制 添加券活动相关数据
     *
     * @param couponThemeEntity          coupon_theme表
     * @param couponThemeStatisticEntity coupon_theme_statistic表
     * @param couponThemeOrgEntityList   coupon_theme_org表
     */
    @Transactional
    @Override
    public void saveCouponThemeRelatedDataWithTx(CouponThemeEntity couponThemeEntity, CouponThemeStatisticEntity couponThemeStatisticEntity, List<CouponThemeOrgEntity> couponThemeOrgEntityList) {
        // 添加coupon_theme表
        couponThemeService.save(couponThemeEntity);
        // 添加coupon_theme_statistic表
        couponThemeStatisticService.getBaseMapper().insert(couponThemeStatisticEntity);
        // 添加coupon_theme_org表
        couponThemeOrgService.insertBatch(couponThemeOrgEntityList);
    }


    @Override
    @Transactional
    public void updateCouponThemeRelatedDataWithTx(CouponThemeEntity couponThemeBean, CouponThemeStatisticEntity couponThemeStatisticBean, Tuple2<List<CouponThemeOrgEntity>, List<CouponThemeOrgEntity>> orgBeans) {
        // coupon_theme表需要更新
        if (Objects.nonNull(couponThemeBean)) {
            couponThemeService.updateById(couponThemeBean);
        }
        // coupon_theme_statistic表需要更新
        if (Objects.nonNull(couponThemeStatisticBean)) {
            couponThemeStatisticService.updateById(couponThemeStatisticBean);
        }
        // coupon_theme_org表需要新增的
        List<CouponThemeOrgEntity> insertOrgBeans = orgBeans.getT1();
        if (CollectionUtil.isNotEmpty(insertOrgBeans)) {
            couponThemeOrgService.insertBatch(insertOrgBeans);
        }
        // coupon_theme_org表需要删除
        List<CouponThemeOrgEntity> deleteOrgBeans = orgBeans.getT2();
        if (CollectionUtil.isNotEmpty(deleteOrgBeans)) {
            List<Long> deleteIds = deleteOrgBeans.stream().map(CouponThemeOrgEntity::getId).collect(Collectors.toList());
            couponThemeOrgService.removeByIds(deleteIds);
        }
    }


    @Override
    @Transactional
    public void updateAfterCheckRelatedDataWithTx(CouponThemeEntity couponThemeUpdateBean, Date oldEffDateEndTime) {
        if (couponThemeUpdateBean == null) {
            return;
        }

        couponThemeService.updateById(couponThemeUpdateBean);
        CouponThemeCache couponThemeCache = new CouponThemeCache();
        // 更新couponThemeCache
        BeanUtil.copyProperties(couponThemeUpdateBean, couponThemeCache);

        CouponThemeCache oldCouponThemeCacheBean = null;
        if (Objects.nonNull(couponThemeUpdateBean.getEffDateEndTime())) {
            Map<String, Object> oldFieldsMap = BeanUtil.beanToMap(couponThemeUpdateBean, false, true);
            LambdaFieldNameSelector<CouponThemeCache> selector = new LambdaFieldNameSelector<>();
            selector.addAll(oldFieldsMap.keySet());
            // update redis前获取下旧的字段属性，回滚用
            oldCouponThemeCacheBean = couponThemeCacheService.getById(couponThemeUpdateBean.getId(), selector);
        }

        couponThemeCacheService.updateById(couponThemeCache);

        if (couponThemeUpdateBean.getEffDateEndTime() != null) {
            try {
                couponEndTimeUpdateAfterCheck(couponThemeUpdateBean);
            } catch (Exception e) {
                log.error("updateAfterCheckRelatedDataWithTx更新coupon表失败，message={}", e.getMessage(), e);
                // 手动回滚couponThemeCache
                oldCouponThemeCacheBean.setEffDateEndTime(oldEffDateEndTime);
                couponThemeCacheService.updateById(oldCouponThemeCacheBean);
                throw e;
            }
        }
    }

    /*
    todo 需要调整 不能根据活动ID更新
     */
    private void couponEndTimeUpdateAfterCheck(CouponThemeEntity couponThemeUpdateBean) {
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setEndTime(couponThemeUpdateBean.getEffDateEndTime());
        couponEntity.setCouponThemeId(couponThemeUpdateBean.getId());
        couponEntity.setUpdateUserid(couponThemeUpdateBean.getUpdateUserid());
        couponEntity.setUpdateUsername(couponThemeUpdateBean.getUpdateUsername());
        LambdaQueryWrapper<CouponEntity> couponQueryWrapper = Wrappers.lambdaQuery(CouponEntity.class);
        couponQueryWrapper
                .eq(CouponEntity::getCouponThemeId, couponThemeUpdateBean.getId());
        couponService.update(couponEntity, couponQueryWrapper);
    }


    /**
     * 更新券活动 数据库和缓存的状态
     *
     * @param couponThemeId 券活动主键
     * @param status        状态 enum
     * @return int
     */
    @Override
    @Transactional
    public int updateCouponThemeAndCacheStatusWithTx(Long couponThemeId, CouponThemeStatus status) {
        int result = couponThemeService.updateCouponThemeStatus(couponThemeId, status);
        // 更新缓存状态
        updateStatusToCouponThemeCache(couponThemeId, status);
        return result;
    }

    /**
     * 更新theme缓存的状态
     *
     * @param couponThemeId 券活动id
     * @param status        需要流转的状态
     */
    private void updateStatusToCouponThemeCache(Long couponThemeId, CouponThemeStatus status) {
        // 设置需要更新的字段
        CouponThemeCache couponThemeCache = new CouponThemeCache();
        couponThemeCache.setId(couponThemeId);
        couponThemeCache.setStatus(status.getStatus());
        couponThemeCacheService.updateById(couponThemeCache);
    }


}
