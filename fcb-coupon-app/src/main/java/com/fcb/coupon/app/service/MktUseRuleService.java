package com.fcb.coupon.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.app.model.entity.MktUseRuleEntity;

import java.util.Set;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 10:39
 */
public interface MktUseRuleService extends IService<MktUseRuleEntity> {
    Set<Long> getApplicableStoreIds(Long couponThemeId);
}
