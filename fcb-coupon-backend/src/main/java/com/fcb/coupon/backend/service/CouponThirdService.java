package com.fcb.coupon.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.backend.model.entity.CouponThirdEntity;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;

import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月11日 18:41:00
 */
public interface CouponThirdService extends IService<CouponThirdEntity> {

    List<CouponThirdEntity> listByThemeId(Long themeId);


}
