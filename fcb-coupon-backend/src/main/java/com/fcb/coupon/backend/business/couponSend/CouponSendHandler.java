package com.fcb.coupon.backend.business.couponSend;

import com.fcb.coupon.backend.model.bo.CouponBatchSendBo;
import com.fcb.coupon.backend.model.dto.CouponSendResult;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.common.exception.BusinessException;

public interface CouponSendHandler {

    /*
    很多接口限制了100条
     */
    int BATCH_SIZE = 100;

    Boolean supports(Integer sendUserType);

    void validate(CouponBatchSendBo couponBatchSendBo,  CouponThemeEntity couponTheme) throws BusinessException;

    CouponSendResult batchSend(CouponBatchSendBo couponBatchSendBo,  CouponThemeEntity couponTheme);
}