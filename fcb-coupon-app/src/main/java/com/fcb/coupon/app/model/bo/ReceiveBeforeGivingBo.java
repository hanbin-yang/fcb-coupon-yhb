package com.fcb.coupon.app.model.bo;

import lombok.Data;

/**
 * @author YangHanBin
 * @date 2021-08-13 8:57
 */
@Data
public class ReceiveBeforeGivingBo {
    /**
     * 劵赠送的主键id
     */
    private Long couponBeforeGiveId;
    /**
     * 领券人用户id
     */
    private Long receiveUserId;
}
