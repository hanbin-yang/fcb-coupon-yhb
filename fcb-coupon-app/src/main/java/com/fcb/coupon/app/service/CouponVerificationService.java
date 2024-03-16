package com.fcb.coupon.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.app.model.bo.OperateCouponDbBo;
import com.fcb.coupon.app.model.entity.CouponVerificationEntity;

/**
 * @author HanBin_Yang
 * @since 2021/6/23 9:16
 */
public interface CouponVerificationService extends IService<CouponVerificationEntity> {
    void operateCoupon4OrderWithTx(OperateCouponDbBo bo);
}
