package com.fcb.coupon.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.app.exception.CouponReceiveErrorCode;
import com.fcb.coupon.app.mapper.CouponMapper;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponUserEntity;
import com.fcb.coupon.app.model.entity.CouponUserStatisticEntity;
import com.fcb.coupon.app.service.CouponService;
import com.fcb.coupon.app.service.CouponThemeStatisticService;
import com.fcb.coupon.app.service.CouponUserService;
import com.fcb.coupon.app.service.CouponUserStatisticService;
import com.fcb.coupon.common.enums.CouponStatusEnum;
import com.fcb.coupon.common.enums.CouponTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

/**
 * 券服务
 *
 * @Author WeiHaiQi
 * @Date 2021-08-13 10:04
 **/
@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, CouponEntity> implements CouponService {
    @Resource
    private CouponUserService couponUserService;
    @Resource
    private CouponThemeStatisticService couponThemeStatisticService;
    @Resource
    private CouponUserStatisticService couponUserStatisticService;

    @Override
    @Transactional
    public void receiveCouponWithTx(CouponEntity couponEntity, CouponUserEntity couponUserEntity, CouponUserStatisticEntity couponUserStatisticEntity) {
        Long couponThemeId = couponEntity.getCouponThemeId();
        // coupon_theme_statistic表
        int updateRows = couponThemeStatisticService.updateSendedCount(couponThemeId, couponUserStatisticEntity.getReceiveCount());
        if (updateRows == 0) {
            log.error("receiveCouponWithTx#updateSendedCount 券活动库存不足: couponThemeId={}", couponThemeId);
            throw new BusinessException(CouponReceiveErrorCode.OUT_OF_STOCK);
        }
        // coupon_user_statistic表
        couponUserStatisticService.updateWithTx(couponUserStatisticEntity);

        //保存券和用户信息
        saveCouponAndUser(couponEntity, couponUserEntity);
    }

    @Transactional
    @Override
    public void saveCouponAndUser(CouponEntity couponEntity, CouponUserEntity couponUserEntity) {
        // coupon_user表
        couponUserService.save(couponUserEntity);
        // coupon表
        switch (CouponTypeEnum.of(couponEntity.getCouponType())) {
            case COUPON_TYPE_VIRTUAL:
                baseMapper.insert(couponEntity);
                break;
            case COUPON_TYPE_THIRD:
                int effRow = this.updateUseStatusById(couponEntity);
                if (effRow == 0) {
                    throw new BusinessException(CouponReceiveErrorCode.RECEIVE_UPDATE_ERROR);
                }
                break;
            default:
                throw new IllegalArgumentException("CouponTypeEnum: [type=" + couponEntity.getCouponType() + "]不存在！");
        }
    }

    /**
     * 更新状态为可使用
     *
     * @param couponEntity couponEntity
     * @return
     */
    @Override
    public int updateUseStatusById(CouponEntity couponEntity) {
        LambdaUpdateWrapper<CouponEntity> wrapper = Wrappers.lambdaUpdate(CouponEntity.class)
                .eq(CouponEntity::getStatus, CouponStatusEnum.STATUS_ISSUE.getStatus())
                .eq(CouponEntity::getId, couponEntity.getId());
        couponEntity.setStatus(CouponStatusEnum.STATUS_USE.getStatus());
        return this.baseMapper.update(couponEntity, wrapper);
    }

    /*
     * @description 更新为已转赠
     * @author 唐陆军
     * @param: couponEntity
     * @date 2021-8-26 18:55
     * @return: int
     */
    @Transactional
    @Override
    public Integer updateGivedStatusById(CouponEntity couponEntity) {
        //更新主表
        LambdaUpdateWrapper<CouponEntity> couponWrapper = Wrappers.lambdaUpdate(CouponEntity.class)
                .eq(CouponEntity::getStatus, CouponStatusEnum.STATUS_USE.getStatus())
                .eq(CouponEntity::getId, couponEntity.getId());
        couponEntity.setStatus(CouponStatusEnum.STATUS_DONATE.getStatus());
        int row = this.baseMapper.update(couponEntity, couponWrapper);
        if (row == 0) {
            return 0;
        }
        //更新领券表
        return couponUserService.updateGivedStatusByCouponId(couponEntity.getId());
    }

    @Override
    public int updateStatusBatchByIds(List<Long> ids, List<Integer> sourceStatusList, Integer targetStatus) {
        couponUserService.updateStatusBatchByIds(ids, sourceStatusList, targetStatus);
        Assert.notEmpty(ids, "ids can not empty");
        Assert.notEmpty(sourceStatusList, "sourceStatusList can not empty");
        Assert.notNull(targetStatus, "targetStatus can not null");

        LambdaUpdateWrapper<CouponEntity> updateWrapper = Wrappers.lambdaUpdate(CouponEntity.class);
        updateWrapper.set(CouponEntity::getStatus, targetStatus);
        if (ids.size() == 1) {
            updateWrapper.eq(CouponEntity::getId, ids.get(0));
        } else {
            updateWrapper.in(CouponEntity::getId, ids.toArray());
        }

        if (sourceStatusList.size() == 1) {
            updateWrapper.eq(CouponEntity::getStatus, sourceStatusList.get(0));
        } else {
            updateWrapper.in(CouponEntity::getStatus, sourceStatusList.toArray());
        }
        return baseMapper.update(null, updateWrapper);
    }

}
