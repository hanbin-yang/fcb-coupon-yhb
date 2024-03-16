package com.fcb.coupon.backend.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CouponImportContext {

    private Integer index;
    private String thirdCouponCode;
    private String thirdCouponPassword;
    private Boolean isFailure;
    private String failureReason;

    private Long createUserid;
    private String createUsername;
}