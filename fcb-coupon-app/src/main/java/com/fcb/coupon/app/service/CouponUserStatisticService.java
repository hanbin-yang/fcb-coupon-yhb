package com.fcb.coupon.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.app.model.entity.CouponUserStatisticEntity;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月05日 19:32:00
 */
public interface CouponUserStatisticService extends IService<CouponUserStatisticEntity> {

    void updateWithTx(CouponUserStatisticEntity entity);
}
