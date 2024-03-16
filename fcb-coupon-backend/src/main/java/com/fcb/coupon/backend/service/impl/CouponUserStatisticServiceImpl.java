package com.fcb.coupon.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.mapper.CouponUserMapper;
import com.fcb.coupon.backend.mapper.CouponUserStatisticMapper;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;
import com.fcb.coupon.backend.model.entity.CouponUserStatisticEntity;
import com.fcb.coupon.backend.service.CouponUserService;
import com.fcb.coupon.backend.service.CouponUserStatisticService;
import com.fcb.coupon.common.enums.CouponStatusEnum;
import com.fcb.coupon.common.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.K;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.CheckForNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月05日 19:32:00
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponUserStatisticServiceImpl extends ServiceImpl<CouponUserStatisticMapper, CouponUserStatisticEntity> implements CouponUserStatisticService {

    @Override
    public List<CouponUserStatisticEntity> listByUserIds(Long themeId, Integer userType, List<String> userIds) {
        LambdaQueryWrapper where = Wrappers.lambdaQuery(CouponUserStatisticEntity.class)
                .eq(CouponUserStatisticEntity::getCouponThemeId, themeId)
                .eq(CouponUserStatisticEntity::getUserType, userType)
                .in(CouponUserStatisticEntity::getUserId, userIds);

        return this.baseMapper.selectList(where);
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public void batchSaveOrUpdate(List<CouponUserEntity> couponUserEntities) {
        if (CollectionUtils.isEmpty(couponUserEntities)) {
            return;
        }
        CouponUserEntity first = couponUserEntities.get(0);
        Long themeId = first.getCouponThemeId();
        Integer userType = first.getUserType();
        Set<String> userIdSet = couponUserEntities.stream().map(m -> m.getUserId()).collect(Collectors.toSet());
        List<CouponUserStatisticEntity> userStatisticEntities = listByUserIds(themeId, userType, new ArrayList<>(userIdSet));
        List<CouponUserEntity> addCouponUserEntities = null;
        List<CouponUserEntity> updateCouponUserEntities = null;
        if (CollectionUtils.isEmpty(userStatisticEntities)) {
            addCouponUserEntities = couponUserEntities;
        } else {
            Set<String> updateUserIdSet = userStatisticEntities.stream().map(m -> m.getUserId()).collect(Collectors.toSet());
            addCouponUserEntities = couponUserEntities.stream().filter(m -> !updateUserIdSet.contains(m.getUserId())).collect(Collectors.toList());
            updateCouponUserEntities = couponUserEntities.stream().filter(m -> updateUserIdSet.contains(m.getUserId())).collect(Collectors.toList());
        }

        //新增
        if (!CollectionUtils.isEmpty(addCouponUserEntities)) {
            List<CouponUserStatisticEntity> addUserStatisticEntities = buildAddUserStatisticEntities(addCouponUserEntities);
            this.saveBatch(addUserStatisticEntities);
        }

        //更新
        if (!CollectionUtils.isEmpty(updateCouponUserEntities)) {
            //分组，一个用户可能发多条
            Map<String, List<CouponUserEntity>> couponUserEntityMap = updateCouponUserEntities.stream().collect(Collectors.groupingBy(CouponUserEntity::getUserId));
            for (CouponUserStatisticEntity userStatisticEntity : userStatisticEntities) {
                List<CouponUserEntity> currentCouponUserEntities = couponUserEntityMap.get(userStatisticEntity.getUserId());
                if (CollectionUtils.isEmpty(currentCouponUserEntities)) {
                    continue;
                }
                int count = currentCouponUserEntities.size();
                setUpdateCount(userStatisticEntity, count);
                this.updateById(userStatisticEntity);
            }
        }
    }

    private void setUpdateCount(CouponUserStatisticEntity userStatisticEntity, Integer count) {
        Date now = new Date();
        //如果是同一天
        if (DateUtils.isTheSameDay(now, userStatisticEntity.getLastReceiveDate())) {
            userStatisticEntity.setTodayCount(userStatisticEntity.getTodayCount() + count);
        } else {
            userStatisticEntity.setTodayCount(count);
        }
        //如果是同一月
        if (DateUtils.isTheSameMonth(now, userStatisticEntity.getLastReceiveDate())) {
            userStatisticEntity.setMonthCount(userStatisticEntity.getMonthCount() + count);
        } else {
            userStatisticEntity.setMonthCount(count);
        }
        userStatisticEntity.setTotalCount(userStatisticEntity.getTotalCount() + count);
        userStatisticEntity.setLastReceiveDate(now);
    }


    private List<CouponUserStatisticEntity> buildAddUserStatisticEntities(List<CouponUserEntity> couponUserEntities) {
        List<CouponUserStatisticEntity> userStatisticEntities = new ArrayList<>();
        //分组，一个用户可能发多条
        Map<String, List<CouponUserEntity>> couponUserEntityMap = couponUserEntities.stream().collect(Collectors.groupingBy(CouponUserEntity::getUserId));
        for (Map.Entry<String, List<CouponUserEntity>> couponUserEntityEntry : couponUserEntityMap.entrySet()) {
            List<CouponUserEntity> currentCouponUserEntities = couponUserEntityEntry.getValue();
            CouponUserEntity first = currentCouponUserEntities.get(0);
            CouponUserStatisticEntity userStatisticEntity = new CouponUserStatisticEntity();
            userStatisticEntity.setCouponThemeId(first.getCouponThemeId());
            userStatisticEntity.setUserType(first.getUserType());
            userStatisticEntity.setUserId(couponUserEntityEntry.getKey());
            userStatisticEntity.setTotalCount(currentCouponUserEntities.size());
            userStatisticEntity.setMonthCount(userStatisticEntity.getTotalCount());
            userStatisticEntity.setTodayCount(userStatisticEntity.getTotalCount());
            userStatisticEntity.setLastReceiveDate(new Date());
            userStatisticEntities.add(userStatisticEntity);
        }
        return userStatisticEntities;
    }
}
