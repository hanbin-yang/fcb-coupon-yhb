package com.fcb.coupon.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fcb.coupon.app.model.bo.OperateCouponDbBo;
import com.fcb.coupon.app.model.entity.CouponVerificationEntity;
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
    int unlockCoupons(List<Long> unlockIds);

    int rebindCoupons(@Param("couponIds") List<Long> couponIds, @Param("entity")CouponVerificationEntity entity);

    void insertOrUpdateBatch(@Param("list") List<CouponVerificationEntity> list);

    void updateBatchByIds(@Param("couponIds") List<Long> couponIds, @Param("entity") CouponVerificationEntity entity);
}
