package com.fcb.coupon.backend.business.couponSend;

import com.fcb.coupon.backend.model.bo.CouponBatchSendBo;
import com.fcb.coupon.backend.model.dto.CouponSendResult;

public interface CouponSendBusiness {

    /*
    手工指定用户营销批量发券
     */
    void manualBatchSend(CouponBatchSendBo bo);

    /*
    主动营销批量发券
     */
    CouponSendResult marketingBatchSend(CouponBatchSendBo bo);

    /*
    活动发券
     */
    CouponSendResult activityBatchSend(CouponBatchSendBo bo);
}
