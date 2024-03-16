package com.fcb.coupon.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.app.exception.CouponGiveErrorCode;
import com.fcb.coupon.app.mapper.CouponGiveMapper;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponGiveEntity;
import com.fcb.coupon.app.model.entity.CouponUserEntity;
import com.fcb.coupon.app.service.CouponGiveService;
import com.fcb.coupon.app.service.CouponService;
import com.fcb.coupon.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月26日 11:46:00
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponGiveServiceImpl extends ServiceImpl<CouponGiveMapper, CouponGiveEntity> implements CouponGiveService {

    private final CouponService couponService;

    /**
     * @description 领券转增优惠券
     * @author 唐陆军
     * @param: sourceCoupon 转赠的优惠券
     * @param: receiveCoupon 接受的优惠券
     * @param: couponGiveEntity 转增记录
     * @date 2021-8-26 14:51
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void receiveGiveCoupon(CouponEntity giveCoupon, CouponEntity receiveCoupon, CouponUserEntity couponUserEntity, CouponGiveEntity couponGiveEntity) {
        //更新优惠券状态为已转赠
        int row = couponService.updateGivedStatusById(giveCoupon);
        if (row == 0) {
            throw new BusinessException(CouponGiveErrorCode.GIVE_COUPON_CHANGE_ERROR);
        }
        //保存新券
        couponService.saveCouponAndUser(receiveCoupon, couponUserEntity);
        //保存转赠记录
        save(couponGiveEntity);
    }
}
