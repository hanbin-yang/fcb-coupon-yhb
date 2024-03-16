package com.fcb.coupon.backend.service;

import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeOrgEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeStatisticEntity;
import com.fcb.coupon.common.enums.CouponThemeStatus;
import reactor.util.function.Tuple2;

import java.util.Date;
import java.util.List;

public interface CouponThemeTxService {

    /**
     * 保存创建券活动所需操作的表
     *
     * @param couponThemeEntity          操作coupon_theme表的数据
     * @param couponThemeStatisticEntity 操作coupon_theme_statistic表的数据
     * @param couponThemeOrgEntityList   操作coupon_theme_org表的数据
     */
    void saveCouponThemeRelatedDataWithTx(CouponThemeEntity couponThemeEntity, CouponThemeStatisticEntity couponThemeStatisticEntity, List<CouponThemeOrgEntity> couponThemeOrgEntityList);


    /**
     * 更新券活动
     *
     * @param couponThemeBean          操作coupon_theme表的数据
     * @param couponThemeStatisticBean 操作coupon_theme_statistic表的数据
     * @param orgBeans                 操作coupon_theme_org表的数据
     */
    void updateCouponThemeRelatedDataWithTx(CouponThemeEntity couponThemeBean, CouponThemeStatisticEntity couponThemeStatisticBean, Tuple2<List<CouponThemeOrgEntity>, List<CouponThemeOrgEntity>> orgBeans);



    int updateCouponThemeAndCacheStatusWithTx(Long couponThemeId, CouponThemeStatus status);

    /*
    更新规则后的事务处理
     */
    void updateAfterCheckRelatedDataWithTx(CouponThemeEntity couponThemeUpdateBean, Date oldEffDateEndTime);

}
