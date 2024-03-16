package com.fcb.coupon.backend.model.dto;

import com.fcb.coupon.common.enums.AsyncStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class CouponSendResult {

    private AsyncStatusEnum status;

    private Integer totalCount;

    private Integer successCount;

    private Integer errorCount;

    private List<CouponSendContext> sendContexts;

}
