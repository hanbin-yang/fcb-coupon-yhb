package com.fcb.coupon.backend.business.couponTheme.impl;

import com.fcb.coupon.backend.business.couponTheme.CouponThemeStatisticBusiness;
import com.fcb.coupon.backend.model.dto.CouponVerificationStatisticDo;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeStatisticEntity;
import com.fcb.coupon.backend.model.param.response.CouponThemeStatisticsResponse;
import com.fcb.coupon.backend.service.CouponThemeService;
import com.fcb.coupon.backend.service.CouponThemeStatisticService;
import com.fcb.coupon.backend.service.CouponVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
@Service
public class CouponThemeStatisticBusinessImpl implements CouponThemeStatisticBusiness {


    private final CouponThemeStatisticService couponThemeStatisticService;
    private final CouponThemeService couponThemeService;
    private final CouponVerificationService couponVerificationService;

    @Override
    public List<CouponThemeStatisticsResponse> listByThemeIds(List<Long> themeIds) {
        if (CollectionUtils.isEmpty(themeIds)) {
            return Collections.EMPTY_LIST;
        }

        List<CouponThemeStatisticEntity> entities = couponThemeStatisticService.listByIds(themeIds);
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.EMPTY_LIST;
        }

        List<CouponThemeEntity> couponThemeEntities = couponThemeService.listByIds(themeIds);
        Map<Long, CouponThemeEntity> couponThemeEntityMap = couponThemeEntities.stream().collect(Collectors.toMap(m -> m.getId(), m -> m));
        List<CouponVerificationStatisticDo> verifications = couponVerificationService.listVerificationCount(themeIds);
        Map<Long, Integer> verificationMap = verifications.stream().collect(Collectors.toMap(m -> m.getCouponThemeId(), m -> m.getCount()));

        List<CouponThemeStatisticsResponse> responses = new ArrayList<>(themeIds.size());
        for (CouponThemeStatisticEntity entity : entities) {
            CouponThemeStatisticsResponse response = new CouponThemeStatisticsResponse();
            responses.add(response);
            response.setId(entity.getCouponThemeId());
            CouponThemeEntity couponThemeEntity = couponThemeEntityMap.get(entity.getCouponThemeId());
            if (couponThemeEntity == null) {
                continue;
            }
            response.setCouponType(couponThemeEntity.getCouponType());
            response.setTotalLimit(entity.getTotalCount());
            response.setDrawedCoupons(entity.getCreatedCount());
            response.setSendedCouopns(entity.getSendedCount());
            response.setCanSendCoupons(entity.getCreatedCount() - entity.getSendedCount());
            Integer verificationCount = verificationMap.get(entity.getCouponThemeId());
            response.setUsedCouopns(verificationCount);
        }

        return responses;
    }
}
