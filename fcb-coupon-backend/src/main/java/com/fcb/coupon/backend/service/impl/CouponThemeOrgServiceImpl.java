package com.fcb.coupon.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.mapper.CouponThemeOrgMapper;
import com.fcb.coupon.backend.service.CouponThemeOrgService;
import com.fcb.coupon.backend.model.entity.CouponThemeOrgEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-06-16 10:07
 */
@Service
public class CouponThemeOrgServiceImpl extends ServiceImpl<CouponThemeOrgMapper, CouponThemeOrgEntity> implements CouponThemeOrgService {
    @Override
    public int insertBatch(List<CouponThemeOrgEntity> list) {
        return baseMapper.insertBatch(list);
    }

    @Override
    public List<CouponThemeOrgEntity> listByCouponThemeIds(List<Long> couponThemeIds) {
        if(CollectionUtils.isEmpty(couponThemeIds)){
            return null;
        }
        LambdaQueryWrapper<CouponThemeOrgEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CouponThemeOrgEntity::getCouponThemeId, couponThemeIds);
        return list(queryWrapper);
    }

    @Override
    public List<CouponThemeOrgEntity> listByCouponThemeId(Long couponThemeId) {
        LambdaQueryWrapper<CouponThemeOrgEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponThemeOrgEntity::getCouponThemeId, couponThemeId);
        return list(queryWrapper);
    }
}
