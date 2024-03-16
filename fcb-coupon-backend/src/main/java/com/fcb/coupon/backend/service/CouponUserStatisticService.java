package com.fcb.coupon.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;
import com.fcb.coupon.backend.model.entity.CouponUserStatisticEntity;
import com.fcb.coupon.backend.model.entity.CouponVerificationEntity;

import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月05日 19:32:00
 */
public interface CouponUserStatisticService extends IService<CouponUserStatisticEntity> {


    List<CouponUserStatisticEntity> listByUserIds(Long themeId, Integer userType, List<String> userIds);

    void batchSaveOrUpdate(List<CouponUserEntity> couponUserEntities);
}
