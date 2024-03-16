package com.fcb.coupon.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.app.mapper.CouponThemeStatisticMapper;
import com.fcb.coupon.app.model.entity.CouponThemeStatisticEntity;
import com.fcb.coupon.app.service.CouponThemeStatisticService;
import org.springframework.stereotype.Service;

/**
 * @author YangHanBin
 * @date 2021-06-16 9:42
 */
@Service
public class CouponThemeStatisticServiceImpl extends ServiceImpl<CouponThemeStatisticMapper, CouponThemeStatisticEntity> implements CouponThemeStatisticService {
    @Override
    public int updateSendedCount(Long couponThemeId, int sendedCount) {
        return baseMapper.updateSendedCount(couponThemeId, sendedCount);
    }
}
