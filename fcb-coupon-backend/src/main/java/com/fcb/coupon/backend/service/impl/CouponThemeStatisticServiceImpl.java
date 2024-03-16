package com.fcb.coupon.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.mapper.CouponThemeStatisticMapper;
import com.fcb.coupon.backend.service.CouponThemeStatisticService;
import com.fcb.coupon.backend.model.entity.CouponThemeStatisticEntity;
import org.springframework.stereotype.Service;

/**
 * @author YangHanBin
 * @date 2021-06-16 9:42
 */
@Service
public class CouponThemeStatisticServiceImpl extends ServiceImpl<CouponThemeStatisticMapper, CouponThemeStatisticEntity> implements CouponThemeStatisticService {
    @Override
    public int incrCreateCountById(Long couponThemeId, int generateCount) {
        return baseMapper.incrCreateCountById(couponThemeId, generateCount);
    }

    @Override
    public int updateSendedCount(Long couponThemeId, int sendedCount) {
        return baseMapper.updateSendedCount(couponThemeId, sendedCount);
    }
}
