package com.fcb.coupon.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fcb.coupon.backend.model.entity.CouponThemeOrgEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 优惠券所属组织商家关联表 Mapper 接口
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
public interface CouponThemeOrgMapper extends BaseMapper<CouponThemeOrgEntity> {

    int insertBatch(@Param("list") List<CouponThemeOrgEntity> list);
}
