package com.fcb.coupon.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fcb.coupon.app.model.entity.CouponUserStatisticEntity;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户领券统计表 Mapper 接口
 * </p>
 *
 * @author 自动生成
 * @since 2021-07-27
 */
public interface CouponUserStatisticMapper extends BaseMapper<CouponUserStatisticEntity> {

    int updateIndividualLimit(CouponUserStatisticEntity entity);

    int updateMonthLimit(CouponUserStatisticEntity entity);

    int updateDayLimit(CouponUserStatisticEntity entity);
}
