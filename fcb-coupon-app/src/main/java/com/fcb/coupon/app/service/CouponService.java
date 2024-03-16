package com.fcb.coupon.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponUserEntity;
import com.fcb.coupon.app.model.entity.CouponUserStatisticEntity;

import java.util.List;

/**
 * 券服务
 *
 * @Author WeiHaiQi
 * @Date 2021-08-13 10:03
 **/
public interface CouponService extends IService<CouponEntity> {

    void receiveCouponWithTx(CouponEntity couponEntity, CouponUserEntity couponUserEntity, CouponUserStatisticEntity couponUserStatisticEntity);

    void saveCouponAndUser(CouponEntity couponEntity, CouponUserEntity couponUserEntity);

    /**
     * 更新状态为可使用
     *
     * @param couponEntity couponEntity
     * @return
     */
    int updateUseStatusById(CouponEntity couponEntity);

    /**
     * @description 更新为已转赠
     * @author 唐陆军
     * @param: couponEntity
     * @date 2021-8-26 18:55
     * @return: int
     */
    Integer updateGivedStatusById(CouponEntity couponEntity);

    /**
     * 批量更新表状态
     *
     * @param ids              主键id
     * @param sourceStatusList 源status
     * @param targetStatus     目标状态
     * @return
     */
    int updateStatusBatchByIds(List<Long> ids, List<Integer> sourceStatusList, Integer targetStatus);


}
