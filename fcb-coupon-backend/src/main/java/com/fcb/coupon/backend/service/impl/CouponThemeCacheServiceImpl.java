package com.fcb.coupon.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.mapper.CouponThemeMapper;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeStatisticEntity;
import com.fcb.coupon.backend.model.query.LambdaFieldNameSelector;
import com.fcb.coupon.backend.service.CouponThemeCacheService;
import com.fcb.coupon.backend.service.CouponThemeStatisticService;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.RedisCacheKeyConstant;
import com.fcb.coupon.common.constant.RedisLockKeyConstant;
import com.fcb.coupon.common.dto.RedisLockResult;
import com.fcb.coupon.common.enums.CouponThemeStatus;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.util.LuaUtil;
import com.fcb.coupon.common.util.RStock.DeductMode;
import com.fcb.coupon.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author YangHanBin
 * @date 2021-06-11 17:03
 */
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class CouponThemeCacheServiceImpl extends ServiceImpl<CouponThemeMapper, CouponThemeEntity> implements CouponThemeCacheService {
    private final RedissonClient redissonClient;
    private final CouponThemeStatisticService couponThemeStatisticService;
    // 2秒
    private final int LOCK_WAIT_SECONDS = 2;

    /**
     * 更新指定字段属性值到couponThemeCache中
     * @param couponThemeCache 不为null的字段将update入缓存
     * @return true/false
     */
    @Override
    public boolean updateById(CouponThemeCache couponThemeCache) {
        if (couponThemeIdIsNull(couponThemeCache.getId())) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_ID_IS_NULL);
        }

        Map<String, Object> map = BeanUtil.beanToMap(couponThemeCache, false, true);
        map.remove("id");
        return updateSelectedFields(couponThemeCache.getId(), map);
    }

    /**
     * createCount字段 += count数量
     * 自加 count 数量
     * @param couponThemeId 券活动id
     * @param count 需要增加的数量
     * @return true/false 如果couponThemId不存在返回null
     */
    @Override
    public Long incrCreatedCountById(Long couponThemeId, long count) {
        if (couponThemeIdIsNull(couponThemeId)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_ID_IS_NULL);
        }
        RMap<String, Object> rMap = getCouponThemeRMap(couponThemeId);
        String fieldName = "createdCount";
        if (rMap.isExists()) {
            Object obj = rMap.addAndGet(fieldName, count);
            return Long.parseLong(String.valueOf(obj));
        }

        // 判定couponThemeId是否存在
        if (couponThemeIdIsMarkedAsNotExist(couponThemeId)) {
            log.error("incrCreateCountById失败，原因为couponThemeId曾经被标记过不存在: couponThemeId={}", couponThemeId);
            return null;
        }
        String lockKeyName = RedisLockKeyConstant.SYNC_COUPON_THEME_DATABASE_TO_CACHE + couponThemeId;
        RedisLockResult<Long> redisLockResult = RedisUtil.executeTryLock(lockKeyName, LOCK_WAIT_SECONDS, () -> {
            // 双重检查
            if (rMap.isExists()) {
                Object innerExistObj = rMap.addAndGet(fieldName, count);
                return Long.parseLong(String.valueOf(innerExistObj));
            }

            CouponThemeCache couponThemeCache = rebuildCouponThemeCache(couponThemeId, rMap);
            if (Objects.isNull(couponThemeCache)) {
                log.error("incrCreateCountById失败，原因为重建缓存后依然判定缓存不存在：couponThemeId={}", couponThemeId);
                return null;
            }
            Object innerObj = rMap.addAndGet(fieldName, count);
            return Long.parseLong(String.valueOf(innerObj));
        });

        if (redisLockResult.isFailure()) {
            log.error("incrCreateCountById失败，原因为获取分布式锁失败: couponThemeId={}", couponThemeId);
            return null;
        }
        return redisLockResult.getObj();
    }

    /**
     * 回滚库存
     * @param couponThemeId 券活动id
     * @param count 需要回滚的数量 如果couponThemId不存在返回null
     * @return 实际回滚数量 如果couponThemId不存在返回null
     */
    @Override
    public Integer rollbackStock(Long couponThemeId, int count) {
        return doRollbackStock(couponThemeId, count);
    }

    /**
     * redis 扣减库存
     * @param couponThemeId 券活动id
     * @param count 扣减数量
     * @return 实际扣减数量 如果couponThemId不存在返回null
     */
    @Override
    public Integer deductStock(Long couponThemeId, int count) {
        return doDeductStock(couponThemeId, count, DeductMode.DECR);
    }

    @Override
    public Integer deductStock(Long couponThemeId, int count, DeductMode mode) {
        return doDeductStock(couponThemeId, count, mode);
    }

    /**
     * 获取coupon_theme表的缓存
     * @param couponThemeId 券活动主键
     * @return 缓存bean 如果couponThemId不存在返回null
     */
    @Override
    public CouponThemeCache getById(Long couponThemeId) {
        Field[] fields = ReflectUtil.getFields(CouponThemeCache.class);
        Set<String> fieldNames = Stream.of(fields).map(Field::getName).filter(name -> !"serialVersionUID".equals(name)).collect(Collectors.toSet());
        return doGetCouponThemeCache(couponThemeId, fieldNames);
    }

    /**
     * 获取coupon_theme表指定字段的缓存
     * @param couponThemeId 券活动主键
     * @param selector 属性选择器
     * @return couponThemeCache缓存数据 如果couponThemId不存在返回null
     */
    @Override
    public CouponThemeCache getById(Long couponThemeId, LambdaFieldNameSelector<CouponThemeCache> selector) {
        return doGetCouponThemeCache(couponThemeId, selector.getFieldNames());
    }

    /**
     * 判断couponThemeId是否被标记为不存在
     * @param couponThemeId 券活动主键id
     * @return true:被标记了 false:未被标记
     */
    @Override
    public boolean couponThemeIdIsMarkedAsNotExist(Long couponThemeId) {
        if (couponThemeIdIsNull(couponThemeId)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_ID_IS_NULL);
        }
        String cacheKey = RedisCacheKeyConstant.NON_EXIST_COUPON_THEME_ID + couponThemeId;
        RBucket<Integer> rBucket = redissonClient.getBucket(cacheKey);
        boolean flag = rBucket.isExists();
        if (flag) {
            log.error("couponThemeIdIsNonExist: couponThemeId={}，在redis中被标记为不存在", couponThemeId);
        }
        return flag;
    }

    /**
     * 标记不存在的couponThemeId到redis
     * @param couponThemeId 券活动主键id
     */
    private void markCouponThemeIdAsNotExist(Long couponThemeId) {
        String cacheKey = RedisCacheKeyConstant.NON_EXIST_COUPON_THEME_ID + couponThemeId;
        RBucket<Integer> rBucket = redissonClient.getBucket(cacheKey);
        rBucket.set(CouponConstant.NO);
        rBucket.expire(3, TimeUnit.MINUTES);
    }

    private Integer doRollbackStock(Long couponThemeId, int count) {
        if (couponThemeIdIsNull(couponThemeId)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_ID_IS_NULL);
        }

        if (count <= 0) {
            throw new IllegalArgumentException("doRollbackStock[count=" + count + "]非法, count必须大于0");
        }

        String cacheKey = RedisCacheKeyConstant.COUPON_THEME_DATABASE + couponThemeId;
        Long realCount = redissonClient.getScript(LongCodec.INSTANCE).evalSha(RScript.Mode.READ_WRITE, LuaUtil.getSha("rollbackCouponThemeStock.lua"), RScript.ReturnType.INTEGER, Collections.singletonList(cacheKey), count);
        if (realCount != null) {
            return realCount.intValue();
        }

        if (couponThemeIdIsMarkedAsNotExist(couponThemeId)) {
            log.error("doRollbackStock失败, 原因为couponThemeId曾经被标记过不存在: couponThemeId={}", couponThemeId);
            return null;
        }

        RMap<String, Object> rMap = getCouponThemeRMap(couponThemeId);
        String lockKeyName = RedisLockKeyConstant.SYNC_COUPON_THEME_DATABASE_TO_CACHE + couponThemeId;
        RedisLockResult<Integer> redisLockResult = RedisUtil.executeTryLock(lockKeyName, LOCK_WAIT_SECONDS, () -> {
            Long innerCount = redissonClient.getScript(LongCodec.INSTANCE).evalSha(RScript.Mode.READ_WRITE, LuaUtil.getSha("rollbackCouponThemeStock.lua"), RScript.ReturnType.INTEGER, Collections.singletonList(cacheKey), count);
            if (innerCount != null) {
                return innerCount.intValue();
            }
            // redis还是不存在
            CouponThemeCache result = rebuildCouponThemeCache(couponThemeId, rMap);
            if (Objects.isNull(result)) {
                return null;
            }

            innerCount = redissonClient.getScript(LongCodec.INSTANCE).evalSha(RScript.Mode.READ_WRITE, LuaUtil.getSha("rollbackCouponThemeStock.lua"), RScript.ReturnType.INTEGER, Collections.singletonList(cacheKey), count);
            // 缓存重建后 仍然拿不到
            if (innerCount == null) {
                log.error("doRollbackStock失败，原因为缓存重建后 仍然判断缓存null, couponThemId={}", couponThemeId);
                throw new BusinessException(CommonErrorCode.GET_COUPON_THEM_CACHE);
            }
            return innerCount.intValue();
        });
        if (redisLockResult.isFailure()) {
            log.error("doRollbackStock失败，原因为获取分布式锁失败: couponThemeId={}", couponThemeId);
            return null;
        }
        return redisLockResult.getObj();

    }

    private Integer doDeductStock(Long couponThemeId, int count, DeductMode mode) {
        if (couponThemeIdIsNull(couponThemeId)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_ID_IS_NULL);
        }

        if (count <= 0) {
            throw new IllegalArgumentException("doDeductStock[count=" + count + "]非法, count必须大于0");
        }

        String cacheKey = RedisCacheKeyConstant.COUPON_THEME_DATABASE + couponThemeId;
        Long realCount = redissonClient.getScript(LongCodec.INSTANCE).evalSha(RScript.Mode.READ_WRITE, LuaUtil.getSha("deductCouponThemeStock.lua"), RScript.ReturnType.INTEGER, Collections.singletonList(cacheKey), count, mode.getFlag());
        if (realCount != null) {
            return realCount.intValue();
        }

        if (couponThemeIdIsMarkedAsNotExist(couponThemeId)) {
            log.error("doDeductStock失败, 原因为couponThemeId曾经被标记过不存在: couponThemeId={}", couponThemeId);
            return null;
        }

        RMap<String, Object> rMap = getCouponThemeRMap(couponThemeId);
        String lockKeyName = RedisLockKeyConstant.SYNC_COUPON_THEME_DATABASE_TO_CACHE + couponThemeId;
        RedisLockResult<Integer> redisLockResult = RedisUtil.executeTryLock(lockKeyName, LOCK_WAIT_SECONDS, () -> {
            Long innerCount = redissonClient.getScript(LongCodec.INSTANCE).evalSha(RScript.Mode.READ_WRITE, LuaUtil.getSha("deductCouponThemeStock.lua"), RScript.ReturnType.INTEGER, Collections.singletonList(cacheKey), count, mode.getFlag());
            if (innerCount != null) {
                return innerCount.intValue();
            }

            CouponThemeCache result = rebuildCouponThemeCache(couponThemeId, rMap);
            if (Objects.isNull(result)) {
                return null;
            }
            innerCount = redissonClient.getScript(LongCodec.INSTANCE).evalSha(RScript.Mode.READ_WRITE, LuaUtil.getSha("deductCouponThemeStock.lua"), RScript.ReturnType.INTEGER, Collections.singletonList(cacheKey), count, mode.getFlag());
            // 缓存重建后 仍然拿不到
            if (innerCount == null) {
                log.error("doDeductStock失败，原因为缓存重建后 仍然判断缓存null, couponThemId={}", couponThemeId);
                throw new BusinessException(CommonErrorCode.GET_COUPON_THEM_CACHE);
            }
            return innerCount.intValue();
        });
        if (redisLockResult.isFailure()) {
            log.error("doDeductStock失败，原因为获取分布式锁失败: couponThemeId={}", couponThemeId);
            return null;
        }
        return redisLockResult.getObj();
    }

    /**
     * 更新指定字段属性值到couponThemeCache中
     * @param couponThemeId 券活动主键
     * @param selectedFilesMap 指定字段map key:字段名 value:要更新的值
     * @return true/false
     */
    private boolean updateSelectedFields(Long couponThemeId, Map<String, Object> selectedFilesMap) {
        // 获取redis 连接对象
        RMap<String, Object> rMap = getCouponThemeRMap(couponThemeId);
        if (rMap.isExists()) {
            setSelectedFieldsToCouponThemeCache(selectedFilesMap, rMap);
            return true;
        }

        if (couponThemeIdIsMarkedAsNotExist(couponThemeId)) {
            log.error("updateSelectedFields失败, 原因为couponThemeId曾经被标记过不存在: couponThemeId={}", couponThemeId);
            return false;
        }

        String lockKeyName = RedisLockKeyConstant.SYNC_COUPON_THEME_DATABASE_TO_CACHE + couponThemeId;
        RedisLockResult<Boolean> redisLockResult = RedisUtil.executeTryLock(lockKeyName, LOCK_WAIT_SECONDS, () -> {
            if (!rMap.isExists()) {
                CouponThemeCache couponThemeCache = rebuildCouponThemeCache(couponThemeId, rMap);
                if (couponThemeCache == null) {
                    log.error("updateSelectedFields失败，原因为券活动id不存在: couponThemeId={}", couponThemeId);
                    return false;
                }
            }
            setSelectedFieldsToCouponThemeCache(selectedFilesMap, rMap);
            return true;
        });
        if (redisLockResult.isFailure()) {
            log.error("updateSelectedFields失败，原因为获取分布式锁失败: couponThemeId={}", couponThemeId);
            return false;
        }
        return redisLockResult.getObj();
    }

    private boolean couponThemeIdIsNull(Long couponThemeId) {
        return couponThemeId == null;
    }

    private CouponThemeCache doGetCouponThemeCache(Long couponThemeId, Set<String> fieldNames) {
        if (couponThemeIdIsNull(couponThemeId)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_ID_IS_NULL);
        }
        CouponThemeCache couponThemeCache = null;
        RMap<String, Object> rMap = getCouponThemeRMap(couponThemeId);
        Map<String, Object> themeCacheMap = rMap.getAll(fieldNames);
        if (!MapUtils.isEmpty(themeCacheMap)) {
            return BeanUtil.fillBeanWithMap(themeCacheMap, new CouponThemeCache(), false);
        }

        // 判定couponThemeId是否存在
        if (couponThemeIdIsMarkedAsNotExist(couponThemeId)) {
            return null;
        }
        String lockKeyName = RedisLockKeyConstant.SYNC_COUPON_THEME_DATABASE_TO_CACHE + couponThemeId;
        RedisLockResult<CouponThemeCache> redisLockResult = RedisUtil.executeTryLock(lockKeyName, LOCK_WAIT_SECONDS, () -> {
            Map<String, Object> cacheMap = rMap.getAll(fieldNames);
            // 双重检查
            if (MapUtils.isEmpty(cacheMap)) {
                return rebuildCouponThemeCache(couponThemeId, rMap);
            }
            return BeanUtil.fillBeanWithMap(cacheMap, new CouponThemeCache(), false);
        });
        if (redisLockResult.isFailure()) {
            log.error("获取券活动缓存失败，原因为获取分布式锁失败: couponThemeId={}", couponThemeId);
            return null;
        }
        couponThemeCache = redisLockResult.getObj();
        return couponThemeCache;
    }

    private CouponThemeCache rebuildCouponThemeCache(Long couponThemeId, RMap<String, Object> rMap) {
        log.info("rebuildCouponThemeCache start: couponThemeId={}", couponThemeId);
        CouponThemeEntity couponTheme = baseMapper.selectById(couponThemeId);
        if (Objects.isNull(couponTheme)) {
            // 标记此couponThemeId为不存在
            markCouponThemeIdAsNotExist(couponThemeId);
            log.error("rebuildCouponThemeCache失败, 原因为数据库中不存在, redis将couponThemeId标记为不存在: couponThemeId={}", couponThemeId);
            return null;
        }
        // 看下此券活动状态，判断是否能加入缓存中
        if (!couponThemeCacheCanAdd(couponTheme.getId(), couponTheme.getStatus())) {
            log.error("rebuildCouponThemeCache失败, 原因为状态[{}]不合法，couponThemeId={}, status={}", CouponThemeStatus.of(couponTheme.getStatus()).getDesc(), couponThemeId, couponTheme.getStatus());
            throw new BusinessException(CouponThemeErrorCode.CAN_NOT_BUILD_CACHE);
        }

        CouponThemeCache couponThemeCache = doAddCouponThemeCache(prepareCouponThemeCacheBean(couponTheme), rMap);
        log.info("rebuildCouponThemeCache ok: couponThemeId={}, couponThemeCache={}", couponThemeId, JSON.toJSONString(couponThemeCache));
        return couponThemeCache;
    }

    /**
     * 券活动是否可以加入缓存 待提交 待审核 审核未通过 是不允许加入缓存的
     * @param couponThemeId 券活动id
     * @param status 券活动状态
     * @return
     */
    private boolean couponThemeCacheCanAdd(Long couponThemeId, Integer status) {
        if (CouponThemeStatus.CREATE.getStatus().equals(status) || CouponThemeStatus.AWAITING_APPROVAL.getStatus().equals(status) || CouponThemeStatus.UN_APPROVE.getStatus().equals(status)) {
            log.error("券活动重建缓存失败，原因为状态是[{}]不可加入缓存, couponThemeId={}, status={}", CouponThemeStatus.of(status).getDesc(), couponThemeId, status);
            return false;
        }
        return true;
    }

    private CouponThemeStatisticEntity getCouponThemeStatisticDbBean(Long couponThemeId) {
        LambdaQueryWrapper<CouponThemeStatisticEntity> statisticQueryWrapper = Wrappers.lambdaQuery(CouponThemeStatisticEntity.class);
        statisticQueryWrapper
                .select(CouponThemeStatisticEntity::getTotalCount,
                        CouponThemeStatisticEntity::getSendedCount,
                        CouponThemeStatisticEntity::getCreatedCount)
                .eq(CouponThemeStatisticEntity::getCouponThemeId, couponThemeId);
        CouponThemeStatisticEntity statisticEntity = couponThemeStatisticService.getBaseMapper().selectOne(statisticQueryWrapper);
        if (Objects.isNull(statisticEntity)) {
            log.error("券活动获取coupon_theme_statistic表数据失败，statisticEntity null!");
            throw new IllegalArgumentException("券活动获取coupon_theme_statistic表数据失败，statisticEntity null!");
        }
        return statisticEntity;
    }

    /**
     * 获取couponThemeCache的redis RMap连接对象
     * @param couponThemeId 券主题 主键
     * @return
     */
    private RMap<String, Object> getCouponThemeRMap(Long couponThemeId) {
        String keyName = RedisCacheKeyConstant.COUPON_THEME_DATABASE + couponThemeId;
        return redissonClient.getMap(keyName, StringCodec.INSTANCE);
    }

    private CouponThemeCache doAddCouponThemeCache(CouponThemeCache couponThemeCache, RMap<String, Object> rMap) {
        Map<String, Object> map = BeanUtil.beanToMap(couponThemeCache, false, true);
        setSelectedFieldsToCouponThemeCache(map, rMap);
        return couponThemeCache;
    }

    private void setSelectedFieldsToCouponThemeCache(Map<String, Object> selectedFieldsMap, RMap<String, Object> rMap) {
        rMap.putAll(selectedFieldsMap);
        // 随机过期时间 次日凌晨1时-2时
        long expireSeconds = RedisUtil.tomorrowRandomExpireSeconds(1, 2, TimeUnit.HOURS);
        rMap.expire(expireSeconds, TimeUnit.SECONDS);
    }

    private CouponThemeCache prepareCouponThemeCacheBean(CouponThemeEntity themeEntity) {
        CouponThemeCache couponThemeCache = new CouponThemeCache();
        BeanUtil.copyProperties(themeEntity, couponThemeCache);

        // 获取当前券活动总可领取券数
        CouponThemeStatisticEntity statisticDbBean = getCouponThemeStatisticDbBean(themeEntity.getId());
        couponThemeCache.setTotalCount(statisticDbBean.getTotalCount());
        couponThemeCache.setCreatedCount(statisticDbBean.getCreatedCount());
        couponThemeCache.setSendedCount(statisticDbBean.getSendedCount());

        return couponThemeCache;
    }
}
