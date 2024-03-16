package com.fcb.coupon.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.mapper.CouponSendLogMapper;
import com.fcb.coupon.backend.model.entity.CouponSendLogEntity;
import com.fcb.coupon.backend.service.CouponSendLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月23日 08:39:00
 */
@Service
public class CouponSendLogServiceImpl extends ServiceImpl<CouponSendLogMapper, CouponSendLogEntity> implements CouponSendLogService {


    @Override
    public List<CouponSendLogEntity> listByThemeIdAndTransIds(Long themeId, List<String> transactionIds) {
        LambdaQueryWrapper wrapper = Wrappers.lambdaQuery(CouponSendLogEntity.class)
                .eq(CouponSendLogEntity::getCouponThemeId, themeId)
                .in(CouponSendLogEntity::getTransactionId, transactionIds);
        return this.baseMapper.selectList(wrapper);
    }
}
