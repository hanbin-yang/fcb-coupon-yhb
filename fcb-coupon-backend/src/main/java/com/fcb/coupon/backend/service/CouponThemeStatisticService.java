package com.fcb.coupon.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.backend.model.entity.CouponThemeStatisticEntity;

/**
 * @author YangHanBin
 * @date 2021-06-16 9:41
 */
public interface CouponThemeStatisticService extends IService<CouponThemeStatisticEntity> {


    int incrCreateCountById(Long couponThemeId, int generateCount);

    /*
    更新发送数量
     */
    int updateSendedCount(Long couponThemeId, int sendedCount);


}
