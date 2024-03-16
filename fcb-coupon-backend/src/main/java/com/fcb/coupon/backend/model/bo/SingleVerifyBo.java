package com.fcb.coupon.backend.model.bo;

import lombok.Data;

/**
 * @author YangHanBin
 * @date 2021-09-13 17:39
 */
@Data
public class SingleVerifyBo {
    /**
     * 核销人id
     */
    private Long verifyUserId;
    /**
     * 核销人name
     */
    private String verifyUsername;
    /**
     * 券码
     */
    private String couponCode;
    /**
     * 明源认购书编号
     */
    private String subscribeCode;
    /**
     * 核销手机号
     */
    private String bindTel;
    /**
     * 核销店铺Id
     */
    private Long usedStoreId;
}
