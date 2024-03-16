package com.fcb.coupon.app.service;

import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.query.LambdaFieldNameSelector;
import com.fcb.coupon.common.util.RStock;

import java.util.List;


/**
 * @author YangHanBin
 * @date 2021-06-11 17:03
 */
public interface CouponThemeCacheService {
    /**
     * 更新指定字段属性值到couponThemeCache中
     * @param couponThemeCache 不为null的字段将update入缓存
     * @return true/false
     */
    boolean updateById(CouponThemeCache couponThemeCache);

    /**
     * createCount增字段自 +=count
     * 自加 count 数量
     * @param couponThemeId 券活动id
     * @param count 需要增加的数量
     * @return count 数量 如果couponThemId不存在返回null
     */
    Long incrCreatedCountById(Long couponThemeId, long count);

    /**
     * 回滚库存
     * @param couponThemeId 券活动id
     * @param count 需要回滚的数量 如果couponThemId不存在返回null
     */
    Integer rollbackStock(Long couponThemeId, int count);

    /**
     * redis 扣减库存
     * @param couponThemeId 券活动id
     * @param count 扣减数量
     * @return 实际扣减数量 如果couponThemId不存在返回null
     */
    Integer deductStock(Long couponThemeId, int count);
    Integer deductStock(Long couponThemeId, int count, RStock.DeductMode mode);

    /**
     * 获取coupon_theme表的缓存
     * @param couponThemeId 券活动主键
     * @return 缓存bean 如果couponThemId不存在返回null
     */
    CouponThemeCache getById(Long couponThemeId);

    /**
     * 获取coupon_theme表指定字段的缓存
     * @param couponThemeId 券活动主键
     * @param selector 属性选择器
     * @return couponThemeCache缓存数据 如果couponThemId不存在返回null
     */
    CouponThemeCache getById(Long couponThemeId, LambdaFieldNameSelector<CouponThemeCache> selector);
    /**
     * 判断couponThemeId是否被标记为不存在
     * @param couponThemeId 券活动主键id
     * @return true:被标记了 false: 未被标记
     */
    boolean couponThemeIdIsMarkedAsNotExist(Long couponThemeId);

    /**
     * @description 查询优惠券活动列表
     * @author 唐陆军
     * @param: themeIds
     * @date 2021-8-27 16:06
     * @return: java.util.List<com.fcb.coupon.app.model.dto.CouponThemeCache>
     */
    List<CouponThemeCache> listCouponTheme(List<Long> themeIds);
}
