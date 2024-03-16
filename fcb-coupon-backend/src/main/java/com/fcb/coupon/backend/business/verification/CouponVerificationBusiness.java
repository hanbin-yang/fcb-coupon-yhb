package com.fcb.coupon.backend.business.verification;

import com.fcb.coupon.backend.model.bo.BatchVerifyBo;
import com.fcb.coupon.backend.model.bo.SingleVerifyBo;

/**
 * @author YangHanBin
 * @date 2021-09-09 10:50
 */
public interface CouponVerificationBusiness {
    void singleVerify(SingleVerifyBo bo);

    void batchVerify(BatchVerifyBo bo);
}
