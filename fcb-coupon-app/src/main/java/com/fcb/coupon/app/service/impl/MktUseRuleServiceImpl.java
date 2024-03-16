package com.fcb.coupon.app.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.app.mapper.MktUseRuleMapper;
import com.fcb.coupon.app.model.entity.MktUseRuleEntity;
import com.fcb.coupon.app.remote.dto.input.InputDto;
import com.fcb.coupon.app.remote.dto.input.OrgInfoDto;
import com.fcb.coupon.app.remote.dto.output.OutputDto;
import com.fcb.coupon.app.remote.dto.output.StoreInfoOutDto;
import com.fcb.coupon.app.remote.ouser.OuserWebFeignClient;
import com.fcb.coupon.app.service.MktUseRuleService;
import com.fcb.coupon.common.constant.InfraConstant;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 10:39
 */
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
@RefreshScope
public class MktUseRuleServiceImpl extends ServiceImpl<MktUseRuleMapper, MktUseRuleEntity> implements MktUseRuleService {
    private final OuserWebFeignClient ouserWebFeignClient;

    private static final int PAGE_SIZE = 200;

    private final Cache<Long, Set<Long>> applicableStoreCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build();
    private final Interner<Long> couponThemeIdIntern = Interners.newWeakInterner();

    @Override
    public Set<Long> getApplicableStoreIds(Long couponThemeId) {
        Set<Long> storeIdSet = applicableStoreCache.getIfPresent(couponThemeId);
        if (CollectionUtils.isEmpty(storeIdSet)) {
            synchronized (couponThemeIdIntern.intern(couponThemeId)) {
                storeIdSet = applicableStoreCache.getIfPresent(couponThemeId);
                if (CollectionUtils.isEmpty(storeIdSet)) {
                    storeIdSet = doGetApplicableStoreIds(couponThemeId);
                    if (storeIdSet == null) {
                        storeIdSet = new HashSet<>();
                    }
                    applicableStoreCache.put(couponThemeId, storeIdSet);
                }
            }
        }
        return storeIdSet;
    }

    private Set<Long> doGetApplicableStoreIds(Long couponThemeId) {
        LambdaQueryWrapper<MktUseRuleEntity> queryWrapper = Wrappers.lambdaQuery(MktUseRuleEntity.class);
        queryWrapper.select(MktUseRuleEntity::getLimitRef)
        .eq(MktUseRuleEntity::getThemeRef, couponThemeId);
        List<MktUseRuleEntity> dbList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dbList)) {
            log.error("券活动没有配置任何组织, couponThemeId={}", couponThemeId);
            return null;
        }
        List<Long> orgIds = dbList.stream().map(MktUseRuleEntity::getLimitRef).distinct().collect(Collectors.toList());
        Set<Long> applicableStoreIdSet = Collections.newSetFromMap(new ConcurrentHashMap<>(512));

        String traceId = MDC.get(InfraConstant.TRACE_ID);
        Lists.partition(orgIds, PAGE_SIZE).parallelStream().forEach(subList -> {
            try {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                // 根据适用组织获取适用楼盘
                List<StoreInfoOutDto> storeInfoOutDtoList = queryChildStores(subList);
                if (storeInfoOutDtoList != null && !storeInfoOutDtoList.isEmpty()) {
                    Set<Long> storeIds = storeInfoOutDtoList.stream().map(StoreInfoOutDto::getStoreId).collect(Collectors.toSet());
                    // 装载适用楼盘ids
                    applicableStoreIdSet.addAll(storeIds);
                }
            } finally {
                MDC.remove(InfraConstant.TRACE_ID);
            }
        });
        return null;
    }

    /**
     * SOA 根据组织ids查询底下所有关联楼盘
     * @param orgList 入参
     * @return 楼盘详情列表
     */
    private List<StoreInfoOutDto> queryChildStores(List<Long> orgList) {
        OrgInfoDto orgInfoDto = new OrgInfoDto();
        orgInfoDto.setIds(orgList);

        InputDto<OrgInfoDto> inputDto = new InputDto<>();
        inputDto.setData(orgInfoDto);
        OutputDto<List<StoreInfoOutDto>> outputDto = ouserWebFeignClient.queryAllStore(inputDto);
        if (Objects.isNull(outputDto)) {
            log.error("根据适用组织查询适用楼盘 error: outputDto null, inputDto={}", JSONUtil.toJsonStr(inputDto));
            return null;
        } else {
            return outputDto.getData();
        }
    }
}
