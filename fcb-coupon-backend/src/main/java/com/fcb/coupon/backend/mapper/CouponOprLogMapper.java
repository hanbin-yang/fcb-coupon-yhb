package com.fcb.coupon.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fcb.coupon.backend.model.entity.CouponOprLogEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CouponOprLogMapper extends BaseMapper<CouponOprLogEntity> {
    void insertBatch(@Param("list") List<CouponOprLogEntity> list);
}