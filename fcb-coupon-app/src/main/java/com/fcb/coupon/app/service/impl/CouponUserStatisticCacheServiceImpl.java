package com.fcb.coupon.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fcb.coupon.app.mapper.CouponUserStatisticMapper;
import com.fcb.coupon.app.model.dto.*;
import com.fcb.coupon.app.model.entity.CouponUserStatisticEntity;
import com.fcb.coupon.app.model.query.LambdaFieldNameSelector;
import com.fcb.coupon.app.service.CouponThemeCacheService;
import com.fcb.coupon.app.service.CouponUserStatisticCacheService;
import com.fcb.coupon.common.constant.RedisCacheKeyConstant;
import com.fcb.coupon.common.constant.RedisLockKeyConstant;
import com.fcb.coupon.common.dto.RedisLockResult;
import com.fcb.coupon.common.constant.PersonalReceiveOverrun;
import com.fcb.coupon.common.enums.YesNoEnum;
import com.fcb.coupon.common.util.DateUtils;
import com.fcb.coupon.common.util.LuaUtil;
import com.fcb.coupon.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.redisson.api.RMap;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.IntegerCodec;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author YangHanBin
 * @date 2021-08-19 14:24
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponUserStatisticCacheServiceImpl implements CouponUserStatisticCacheService {
    private final CouponThemeCacheService couponThemeCacheService;
    private final RedissonClient redissonClient;
    private final CouponUserStatisticMapper baseMapper;
    // 2秒
    private final int LOCK_WAIT_SECONDS = 2;

    /**
     * 根据联合主键获取个人领券信息
     * @param couponThemeId 券活动id
     * @param userId 用户id 必须确保真实性，否则数据库出现垃圾数据
     * @param userType 用户类型
     * @return 个人领券信息
     */
    @Override
    public CouponUserStatisticCache getByUnionKey(Long couponThemeId, String userId, Integer userType) {
        RMap<String, Object> rMap = getRMap(couponThemeId, userId, userType);
        Set<String> allFields = Stream
                .of(CouponUserStatisticCache.FIELDS.values())
                .map(CouponUserStatisticCache.FIELDS::getName)
                .collect(Collectors.toSet());
        Map<String, Object> cacheMap = rMap.getAll(allFields);
        if (!MapUtils.isEmpty(cacheMap)) {
            return convertCacheMapToCacheBean(cacheMap);
        }

        // 判定couponThemeId是否存在
        if (couponThemeCacheService.couponThemeIdIsMarkedAsNotExist(couponThemeId)) {
            log.error("CouponUserStatisticCache getByUnionKey error: 原因券活动被标记过不存在, couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
            return null;
        }

        String lockKey = MessageFormat.format(RedisLockKeyConstant.SYNC_COUPON_USER_STATISTIC_DB_TO_CACHE, String.valueOf(couponThemeId), userId, userType);
        RedisLockResult<CouponUserStatisticCache> redisLockResult = RedisUtil.executeTryLock(lockKey, LOCK_WAIT_SECONDS, () -> {
            Map<String, Object> internalCacheMap = rMap.getAll(allFields);
            if (!MapUtils.isEmpty(internalCacheMap)) {
                return convertCacheMapToCacheBean(cacheMap);
            }
            CouponUserStatisticCache couponUserStatisticCache = rebuildCouponUserStatisticCache(couponThemeId, userId, userType, rMap);
            if (couponUserStatisticCache == null) {
                log.error("getByUnionKey rebuildCouponUserStatisticCache error: couponUserStatisticCache null, couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
            }
            return couponUserStatisticCache;
        });
        if (redisLockResult.isFailure()) {
            log.error("CouponUserStatisticCache getByUnionKey 获取券活动缓存失败，原因为获取分布式锁失败: couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
            return null;
        }

        return redisLockResult.getObj();
    }

    /**
     * 扣减个人可领券数
     * @param dto dto
     * @return 领券备忘录，后续回滚用
     */
    @Override
    public UserReceiveMemento deductStock(PersonalReceiveDo dto) {
        Long couponThemeId = dto.getCouponThemeId();
        String userId = dto.getUserId();
        Integer userType = dto.getUserType();

        validateUnionKey(couponThemeId, userId, userType);

        Date receiveTime = dto.getReceiveTime();
        Date beginOfMonth = DateUtil.beginOfMonth(receiveTime);
        Date beginOfDay = DateUtil.beginOfDay(receiveTime);

        int totalLimit = dto.getTotalLimit() == null || dto.getTotalLimit() == 0 ? Integer.MAX_VALUE : dto.getTotalLimit();
        int monthLimit = dto.getMonthLimit() == null || dto.getMonthLimit() == 0 ? Integer.MAX_VALUE : dto.getMonthLimit();
        int dayLimit = dto.getDayLimit() == null || dto.getDayLimit() == 0 ? Integer.MAX_VALUE : dto.getDayLimit();
        Object[] objects = {
                dto.getReceiveCount(),
                totalLimit,
                monthLimit,
                dayLimit,
                receiveTime.getTime(),
                beginOfMonth.getTime(),
                beginOfDay.getTime()
        };

        String cacheKey = MessageFormat.format(RedisCacheKeyConstant.COUPON_USER_STATISTIC_DB, String.valueOf(couponThemeId), userId, userType);
        String resultObj = redissonClient.getScript(StringCodec.INSTANCE).evalSha(RScript.Mode.READ_WRITE, LuaUtil.getSha("deductCouponUserReceiveStock.lua"), RScript.ReturnType.VALUE, Collections.singletonList(cacheKey), objects);
        if (resultObj != null) {
            return getUserReceiveMemento(dto, resultObj);
        }

        if (couponThemeCacheService.couponThemeIdIsMarkedAsNotExist(couponThemeId)) {
            log.error("deductCouponUserReceiveStock error: 原因券活动被标记过不存在, couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
            return null;
        }

        String lockKey = MessageFormat.format(RedisLockKeyConstant.SYNC_COUPON_USER_STATISTIC_DB_TO_CACHE, String.valueOf(couponThemeId), userId, userType);
        RedisLockResult<UserReceiveMemento> redisLockResult = RedisUtil.executeTryLock(lockKey, LOCK_WAIT_SECONDS, () -> {
            String resultObjInternal = redissonClient.getScript(StringCodec.INSTANCE).evalSha(RScript.Mode.READ_WRITE, LuaUtil.getSha("deductCouponUserReceiveStock.lua"), RScript.ReturnType.VALUE, Collections.singletonList(cacheKey), objects);
            if (resultObjInternal != null) {
                return getUserReceiveMemento(dto, resultObjInternal);
            }

            RMap<String, Object> rMap = getRMap(couponThemeId, userId, userType);
            CouponUserStatisticCache couponUserStatisticCache = rebuildCouponUserStatisticCache(couponThemeId, userId, userType, rMap);
            if (couponUserStatisticCache == null) {
                log.error("deductCouponUserReceiveStock error: couponUserStatisticCache null, couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
                return null;
            }

            resultObjInternal = redissonClient.getScript(StringCodec.INSTANCE).evalSha(RScript.Mode.READ_WRITE, LuaUtil.getSha("deductCouponUserReceiveStock.lua"), RScript.ReturnType.VALUE, Collections.singletonList(cacheKey), objects);
            if (resultObjInternal == null) {
                log.error("deductCouponUserReceiveStock失败，原因为缓存重建后 仍然判断缓存null, couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
                return null;
            }
            return getUserReceiveMemento(dto, resultObjInternal);
        });

        if (redisLockResult.isFailure()) {
            log.error("deductCouponUserReceiveStock 获取锁失败，couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
            return null;
        }

        return redisLockResult.getObj();
    }

    /**
     * 回滚个人可领券数
     * @param memento 扣库存得到的备忘录实体
     */
    @Override
    public Boolean rollbackStock(UserReceiveMemento memento) {
        Long couponThemeId = memento.getCouponThemeId();
        String userId = memento.getUserId();
        Integer userType = memento.getUserType();
        validateUnionKey(couponThemeId, userId, userType);

        if (Objects.equals(memento.getRealReceiveCount(), PersonalReceiveOverrun.OUT_TOTAL_LIMIT)) {
            log.warn("回滚个人领券限制失败， 原因为deductStock时超出个人总领券限制，couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
            return false;
        } else if (Objects.equals(memento.getRealReceiveCount(), PersonalReceiveOverrun.OUT_MONTH_LIMIT)) {
            log.warn("回滚个人领券限制失败， 原因为deductStock时超出每月领券限制，couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
            return false;
        } else if (Objects.equals(memento.getRealReceiveCount(), PersonalReceiveOverrun.OUT_DAY_LIMIT)) {
            log.warn("回滚个人领券限制失败， 原因为deductStock时超出每天领券限制，couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
            return false;
        }

        Object[] objects = {
                memento.getRealReceiveCount(),
                memento.getOldMonthCount(),
                memento.getOldDayCount(),
                memento.getCurrReceiveDate(),
                memento.getCurrReceiveDate(),
        };

        String cacheKey = MessageFormat.format(RedisCacheKeyConstant.COUPON_USER_STATISTIC_DB, String.valueOf(couponThemeId), userId, userType);
        Boolean result = redissonClient.getScript(IntegerCodec.INSTANCE).evalSha(RScript.Mode.READ_WRITE, LuaUtil.getSha("rollbackCouponUserReceiveStock.lua"), RScript.ReturnType.BOOLEAN, Collections.singletonList(cacheKey), objects);
        if (result != null) {
            return result;
        }

        if (couponThemeCacheService.couponThemeIdIsMarkedAsNotExist(couponThemeId)) {
            log.error("rollbackCouponUserReceiveStock error: 原因券活动被标记过不存在, couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
            return null;
        }

        String lockKey = MessageFormat.format(RedisLockKeyConstant.SYNC_COUPON_USER_STATISTIC_DB_TO_CACHE, String.valueOf(couponThemeId), userId, userType);
        RedisLockResult<Boolean> redisLockResult = RedisUtil.executeTryLock(lockKey, LOCK_WAIT_SECONDS, () -> {
            Boolean resultInternal = redissonClient.getScript(IntegerCodec.INSTANCE).evalSha(RScript.Mode.READ_WRITE, LuaUtil.getSha("rollbackCouponUserReceiveStock.lua"), RScript.ReturnType.BOOLEAN, Collections.singletonList(cacheKey), objects);
            if (resultInternal != null) {
                return resultInternal;
            }

            RMap<String, Object> rMap = getRMap(couponThemeId, userId, userType);
            CouponUserStatisticCache couponUserStatisticCache = rebuildCouponUserStatisticCache(couponThemeId, userId, userType, rMap);
            if (couponUserStatisticCache == null) {
                log.error("rollbackCouponUserReceiveStock error: couponUserStatisticCache null, couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
                return null;
            }
            resultInternal = redissonClient.getScript(IntegerCodec.INSTANCE).evalSha(RScript.Mode.READ_WRITE, LuaUtil.getSha("rollbackCouponUserReceiveStock.lua"), RScript.ReturnType.BOOLEAN, Collections.singletonList(cacheKey), objects);
            if (resultInternal == null) {
                log.error("rollbackCouponUserReceiveStock失败，原因为缓存重建后 仍然判断缓存null, couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
                return null;
            }
            return resultInternal;
        });

        if (redisLockResult.isFailure()) {
            log.error("rollbackCouponUserReceiveStock 获取锁失败，couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
            return null;
        }

        return redisLockResult.getObj();
    }

    @Override
    public void deleteByUnionKey(Long couponThemeId, String userId, Integer userType) {
        RMap<String, Object> rMap = getRMap(couponThemeId, userId, userType);
        rMap.delete();
    }

    private UserReceiveMemento getUserReceiveMemento(PersonalReceiveDo dto, String objStr) {
        UserReceiveMemento memento = JSON.parseObject(objStr, UserReceiveMemento.class);
        memento.setCouponThemeId(dto.getCouponThemeId());
        memento.setUserId(dto.getUserId());
        memento.setUserType(dto.getUserType());
        memento.setCurrReceiveDate(dto.getReceiveTime().getTime());
        return memento;
    }

    private void validateUnionKey(Long couponThemeId, String userId, Integer userType) {
        if (Objects.isNull(couponThemeId)) {
            throw new IllegalArgumentException("couponThemeId不能为空");
        }
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId不能为空");
        }
        if (Objects.isNull(userType)) {
            throw new IllegalArgumentException("userType不能为空");
        }
    }

    private CouponUserStatisticCache convertCacheMapToCacheBean(Map<String, Object> cacheMap) {
        CouponUserStatisticCache cacheBean = BeanUtil.fillBeanWithMap(cacheMap, new CouponUserStatisticCache(), false);
        Long lastReceiveDate = cacheBean.getLastReceiveDate();
        Date now = new Date();
        if (DateUtils.isTheSameMonth(now, new Date(lastReceiveDate))) {
            cacheBean.setMonthCount(0);
            cacheBean.setTodayCount(0);
        } else if (DateUtils.isTheSameDay(now, new Date(lastReceiveDate))) {
            cacheBean.setTodayCount(0);
        }
        return cacheBean;
    }

    private CouponUserStatisticCache rebuildCouponUserStatisticCache(Long couponThemeId, String userId, Integer userType, RMap<String, Object> rMap) {
        log.info("rebuildCouponUserStatisticCache start: couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
        LambdaFieldNameSelector<CouponThemeCache> selector = new LambdaFieldNameSelector<>(CouponThemeCache.class);
        selector.select(CouponThemeCache::getId);
        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(couponThemeId, selector);
        if (Objects.isNull(couponThemeCache)) {
            log.error("rebuildCouponUserStatisticCache失败，原因为券活动id不存在 couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
            return null;
        }
        LambdaQueryWrapper<CouponUserStatisticEntity> wrapper = Wrappers.lambdaQuery(CouponUserStatisticEntity.class);
        wrapper
                .select(
                        CouponUserStatisticEntity::getTotalCount,
                        CouponUserStatisticEntity::getMonthCount,
                        CouponUserStatisticEntity::getTodayCount,
                        CouponUserStatisticEntity::getLastReceiveDate)
                .eq(CouponUserStatisticEntity::getCouponThemeId, couponThemeId)
                .eq(CouponUserStatisticEntity::getUserId, userId)
                .eq(CouponUserStatisticEntity::getUserType, userType);
        CouponUserStatisticEntity dbBean = baseMapper.selectOne(wrapper);
        CouponUserStatisticCache cacheBean = new CouponUserStatisticCache();
        if (Objects.isNull(dbBean)) {
            log.warn("rebuildCouponUserStatisticCache查询表返回null, 即将insert一条, couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
            Date now = new Date();
            CouponUserStatisticEntity entity = new CouponUserStatisticEntity()
                    .setId(RedisUtil.generateId())
                    .setCouponThemeId(couponThemeId)
                    .setUserId(userId)
                    .setUserType(userType)
                    .setCreateTime(now)
                    .setLastReceiveDate(now)
                    .setUpdateTime(now)
                    .setTodayCount(YesNoEnum.NO.getValue())
                    .setMonthCount(YesNoEnum.NO.getValue())
                    .setTotalCount(YesNoEnum.NO.getValue());
            baseMapper.insert(entity);
            log.warn("rebuildCouponUserStatisticCache insert一条success, couponThemeId={}, userId={}, userType={}", couponThemeId, userId, userType);
            cacheBean.setTotalCount(YesNoEnum.NO.getValue());
            cacheBean.setMonthCount(YesNoEnum.NO.getValue());
            cacheBean.setTodayCount(YesNoEnum.NO.getValue());
            cacheBean.setLastReceiveDate(now.getTime());
        } else {
            cacheBean.setTotalCount(Optional.ofNullable(dbBean.getTotalCount()).orElse(0));
            cacheBean.setMonthCount(Optional.ofNullable(dbBean.getMonthCount()).orElse(0));
            cacheBean.setTodayCount(Optional.ofNullable(dbBean.getTodayCount()).orElse(0));
            cacheBean.setLastReceiveDate(dbBean.getLastReceiveDate().getTime());
        }

        setSelectedFieldsToCache(cacheBean, rMap);
        log.info("rebuildCouponUserStatisticCache ok: couponThemeId={}, userId={}, userType={}, cacheBean={}", couponThemeId, userId, userType, JSON.toJSONString(cacheBean));
        return cacheBean;
    }

    private void setSelectedFieldsToCache(CouponUserStatisticCache cacheBean, RMap<String, Object> rMap) {
        Map<String, Object> map = BeanUtil.beanToMap(cacheBean, false, true);
        rMap.putAll(map);
        // 随机过期时间
        long expireSeconds = RedisUtil.randomExpireSeconds(3, 4, TimeUnit.DAYS);
        rMap.expire(expireSeconds, TimeUnit.SECONDS);
    }

    private RMap<String, Object> getRMap(Long couponThemeId, String userId, Integer userType) {
        String cacheKey = MessageFormat.format(RedisCacheKeyConstant.COUPON_USER_STATISTIC_DB, String.valueOf(couponThemeId), userId, userType);
        return redissonClient.getMap(cacheKey, StringCodec.INSTANCE);
    }
}
