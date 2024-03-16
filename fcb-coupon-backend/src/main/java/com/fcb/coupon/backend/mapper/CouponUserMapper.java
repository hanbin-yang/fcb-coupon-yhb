package com.fcb.coupon.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fcb.coupon.backend.model.dto.CouponUserConditionDto;
import com.fcb.coupon.backend.model.dto.CouponUserTotalDto;
import com.fcb.coupon.backend.model.dto.SendedAndUsedCouponDto;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * 劵表 Mapper 接口
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
public interface CouponUserMapper extends BaseMapper<CouponUserEntity> {

    void batchSave(@Param("list") List<CouponUserEntity> couponUserEntities);

    /**
     * 通过券活动id及状态统计已领取和已使用的券数
     *
     * @param params
     * @return
     */
    List<SendedAndUsedCouponDto> countSendedAndUsedCoupons(Map<String, Object> params);
}
