package com.fcb.coupon.app.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.app.model.entity.CouponThemeStatisticEntity;

/**
 * @author YangHanBin
 * @date 2021-06-16 9:41
 */
public interface CouponThemeStatisticService extends IService<CouponThemeStatisticEntity> {

    int updateSendedCount(Long couponThemeId, int count);
}
