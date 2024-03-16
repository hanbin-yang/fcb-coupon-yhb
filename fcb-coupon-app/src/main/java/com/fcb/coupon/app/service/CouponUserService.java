package com.fcb.coupon.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.app.model.PageDto;
import com.fcb.coupon.app.model.bo.CouponUserListBo;
import com.fcb.coupon.app.model.entity.CouponUserEntity;

import java.util.List;

/**
 * 券用户服务
 *
 * @Author WeiHaiQi
 * @Date 2021-06-22 10:21
 **/
public interface CouponUserService extends IService<CouponUserEntity> {


    CouponUserEntity get(String userId, Integer userType, Long couponId);

    Integer countByEffective(CouponUserListBo query);

    List<CouponUserEntity> listByEffective(CouponUserListBo query, PageDto pageDto);

    Integer countByExpired(CouponUserListBo query);

    List<CouponUserEntity> listByExpired(CouponUserListBo query, PageDto pageDto);


    Integer updateGivedStatusByCouponId(Long couponId);

    /**
     * 批量更新表状态
     *
     * @param ids              主键id
     * @param sourceStatusList 源status
     * @param targetStatus     目标状态
     * @return
     */
    Integer updateStatusBatchByIds(List<Long> ids, List<Integer> sourceStatusList, Integer targetStatus);

}
