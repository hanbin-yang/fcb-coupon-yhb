package com.fcb.coupon.backend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.mapper.CouponThirdMapper;
import com.fcb.coupon.backend.mapper.CouponUserMapper;
import com.fcb.coupon.backend.model.entity.CouponThirdEntity;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;
import com.fcb.coupon.backend.service.CouponThirdService;
import com.fcb.coupon.backend.service.CouponUserService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月11日 18:42:00
 */
@Service
public class CouponThirdServiceImpl extends ServiceImpl<CouponThirdMapper, CouponThirdEntity> implements CouponThirdService {

    @Override
    public List<CouponThirdEntity> listByThemeId(Long themeId) {
        return baseMapper.selectList(Wrappers.lambdaQuery(CouponThirdEntity.class).eq(CouponThirdEntity::getCouponThemeId, themeId));
    }
}
