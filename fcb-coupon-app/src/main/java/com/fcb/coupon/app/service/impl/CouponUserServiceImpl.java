package com.fcb.coupon.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.app.mapper.CouponUserMapper;
import com.fcb.coupon.app.model.PageDto;
import com.fcb.coupon.app.model.bo.CouponUserListBo;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponUserEntity;
import com.fcb.coupon.app.service.CouponUserService;
import com.fcb.coupon.common.enums.CouponStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 券用户服务实现
 *
 * @Author WeiHaiQi
 * @Date 2021-06-22 10:21
 **/
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponUserServiceImpl extends ServiceImpl<CouponUserMapper, CouponUserEntity> implements CouponUserService {

    @Override
    public CouponUserEntity get(String userId, Integer userType, Long couponId) {
        LambdaUpdateWrapper<CouponUserEntity> wrapper = Wrappers.lambdaUpdate(CouponUserEntity.class)
                .eq(CouponUserEntity::getUserId, userId)
                .eq(CouponUserEntity::getUserType, userType)
                .eq(CouponUserEntity::getCouponId, couponId);
        return this.baseMapper.selectOne(wrapper);
    }

    @Override
    public Integer countByEffective(CouponUserListBo query) {
        return this.baseMapper.countByEffective(query);
    }

    @Override
    public List<CouponUserEntity> listByEffective(CouponUserListBo query, PageDto pageDto) {
        return this.baseMapper.listByEffective(query, pageDto.getStart(), pageDto.getPage());
    }

    @Override
    public Integer countByExpired(CouponUserListBo query) {
        return this.baseMapper.countByExpired(query);
    }

    @Override
    public List<CouponUserEntity> listByExpired(CouponUserListBo query, PageDto pageDto) {
        return this.baseMapper.listByExpired(query, pageDto.getStart(), pageDto.getPage());
    }

    @Override
    public Integer updateGivedStatusByCouponId(Long couponId) {
        LambdaUpdateWrapper<CouponUserEntity> wrapper = Wrappers.lambdaUpdate(CouponUserEntity.class)
                .eq(CouponUserEntity::getStatus, CouponStatusEnum.STATUS_USE.getStatus())
                .eq(CouponUserEntity::getCouponId, couponId);
        CouponUserEntity couponUserEntity = new CouponUserEntity();
        couponUserEntity.setStatus(CouponStatusEnum.STATUS_DONATE.getStatus());
        return this.baseMapper.update(couponUserEntity, wrapper);
    }

    @Override
    public Integer updateStatusBatchByIds(List<Long> ids, List<Integer> sourceStatusList, Integer targetStatus) {
        Assert.notEmpty(ids, "ids can not empty");
        Assert.notEmpty(sourceStatusList, "sourceStatusList can not empty");
        Assert.notNull(targetStatus, "targetStatus can not null");

        LambdaUpdateWrapper<CouponUserEntity> updateWrapper = Wrappers.lambdaUpdate(CouponUserEntity.class);
        updateWrapper.set(CouponUserEntity::getStatus, targetStatus);
        if (ids.size() == 1) {
            updateWrapper.eq(CouponUserEntity::getCouponId, ids.get(0));
        } else {
            updateWrapper.in(CouponUserEntity::getCouponId, ids.toArray());
        }

        if (sourceStatusList.size() == 1) {
            updateWrapper.eq(CouponUserEntity::getStatus, sourceStatusList.get(0));
        } else {
            updateWrapper.in(CouponUserEntity::getStatus, sourceStatusList.toArray());
        }
        return baseMapper.update(null, updateWrapper);
    }
}
