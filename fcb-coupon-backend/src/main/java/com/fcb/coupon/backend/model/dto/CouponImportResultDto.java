package com.fcb.coupon.backend.model.dto;

import com.fcb.coupon.common.enums.AsyncStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/*
优惠券导入结果
 */
@Builder
@Data
public class CouponImportResultDto {

    private AsyncStatusEnum asyncStatusEnum;

    private Integer totalCount;

    private Integer successCount;

    private Integer errorCount;

    private List<CouponImportContext> couponImportContexts;

}
