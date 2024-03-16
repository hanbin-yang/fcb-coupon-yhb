package com.fcb.coupon.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fcb.coupon.app.model.entity.CouponThemeStatisticEntity;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 劵活动统计表 Mapper 接口
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
public interface CouponThemeStatisticMapper extends BaseMapper<CouponThemeStatisticEntity> {

    /**
     * 更新发送的总数
     * @param couponThemeId
     * @param sendedCount
     * @return
     */
    int updateSendedCount(@Param("couponThemeId") Long couponThemeId, @Param("sendedCount") int sendedCount);
}
