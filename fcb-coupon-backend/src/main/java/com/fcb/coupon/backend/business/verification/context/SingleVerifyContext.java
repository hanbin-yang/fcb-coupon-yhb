package com.fcb.coupon.backend.business.verification.context;

import lombok.Data;

/**
 * @author YangHanBin
 * @date 2021-09-09 14:57
 */
@Data
public class SingleVerifyContext {
    private String couponCode;

    private String subscribeCode;

    private String bindTel;

    private Long usedStoreId;

    private Long verifyUserId;

    private String verifyUsername;
}
