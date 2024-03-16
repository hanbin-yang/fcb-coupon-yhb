package com.fcb.coupon.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.app.mapper.CouponGenerateBatchMapper;
import com.fcb.coupon.app.model.entity.CouponGenerateBatchEntity;
import com.fcb.coupon.app.model.param.response.MarketingCouponUseCountResponse;
import com.fcb.coupon.app.service.CouponGenerateBatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CouponGenerateBatchServiceImpl extends ServiceImpl<CouponGenerateBatchMapper, CouponGenerateBatchEntity> implements CouponGenerateBatchService {


//    @Override
//    public MarketingCouponUseCountResponse countMarketingCouponByTaskId(Long taskId) {
//        LambdaQueryWrapper<CouponGenerateBatchEntity> queryWrapper = Wrappers.lambdaQuery(CouponGenerateBatchEntity.class);
//        queryWrapper.eq(CouponGenerateBatchEntity::getId, taskId);
////        couponUserQuery.in(CouponGenerateBatchEntity::getType, ""主动营销发券类型"");
//
//        CouponGenerateBatchEntity couponGenerateBatch = this.getBaseMapper().selectOne(queryWrapper);
//
//        MarketingCouponUseCountResponse result = new MarketingCouponUseCountResponse();
//        result.setBatchNo(couponGenerateBatch.getId()+"");
//        result.setCouponSuccessCount(Long.valueOf(couponGenerateBatch.getSuccessRecord()));
//
//        return result;
//    }
}
