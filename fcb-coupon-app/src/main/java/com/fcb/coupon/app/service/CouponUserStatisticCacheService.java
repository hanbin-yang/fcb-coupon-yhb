package com.fcb.coupon.app.service;

import com.fcb.coupon.app.model.dto.CouponUserStatisticCache;
import com.fcb.coupon.app.model.dto.PersonalReceiveDo;
import com.fcb.coupon.app.model.dto.UserReceiveMemento;

/**
 * @author YangHanBin
 * @date 2021-08-19 14:24
 */
public interface CouponUserStatisticCacheService {
    /**
     * 根据联合主键获取个人领券信息
     * @param couponThemeId 券活动id
     * @param userId 用户id 必须确保真实性，否则数据库出现垃圾数据
     * @param userType 用户类型
     * @return 个人领券信息 如果券活动不存在返回null
     */
    CouponUserStatisticCache getByUnionKey(Long couponThemeId, String userId, Integer userType);
    /**
     * 扣减个人可领券数
     * @param dto dto
     * @return 领券备忘录，后续回滚用 如果券活动不存在返回null
     */
    UserReceiveMemento deductStock(PersonalReceiveDo dto);
    /**
     * 回滚个人可领券数
     * @param memento 扣库存得到的备忘录实体
     */
    Boolean rollbackStock(UserReceiveMemento memento);

    /**
     * 删除缓存
     * @param couponThemeId 券活动id
     * @param userId 用户id
     * @param userType 用户类型
     */
    void deleteByUnionKey(Long couponThemeId, String userId, Integer userType);
}
