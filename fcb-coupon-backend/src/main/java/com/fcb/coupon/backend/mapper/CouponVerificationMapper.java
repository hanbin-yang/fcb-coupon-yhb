package com.fcb.coupon.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fcb.coupon.backend.model.dto.CouponVerificationStatisticDo;
import com.fcb.coupon.backend.model.entity.CouponVerificationEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 劵使用表 Mapper 接口
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
public interface CouponVerificationMapper extends BaseMapper<CouponVerificationEntity> {
    void insertOrUpdate(@Param("entity") CouponVerificationEntity entity);

    List<CouponVerificationStatisticDo> listVerificationCount(@Param("list") List<Long> couponThemeId);
}
