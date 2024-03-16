package com.fcb.coupon.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.mapper.CouponUserMapper;
import com.fcb.coupon.backend.model.dto.CouponUserConditionDto;
import com.fcb.coupon.backend.model.dto.CouponUserTotalDto;
import com.fcb.coupon.backend.model.dto.SendedAndUsedCouponDto;
import com.fcb.coupon.backend.service.CouponUserService;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
    public void batchSave(List<CouponUserEntity> couponUserEntities) {
        this.baseMapper.batchSave(couponUserEntities);
    }


    @Override
    public List<SendedAndUsedCouponDto> countSendedAndUsedCoupons(Map<String, Object> params) {
        return this.baseMapper.countSendedAndUsedCoupons(params);
    }
}
