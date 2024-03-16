package com.fcb.coupon.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.backend.model.entity.CouponThemeOrgEntity;

import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-06-16 9:57
 */
public interface CouponThemeOrgService extends IService<CouponThemeOrgEntity> {
    int insertBatch(List<CouponThemeOrgEntity> list);
    List<CouponThemeOrgEntity> listByCouponThemeIds(List<Long> couponThemeIds);
    List<CouponThemeOrgEntity> listByCouponThemeId(Long couponThemeId);
}
