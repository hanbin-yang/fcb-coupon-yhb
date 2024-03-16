package com.fcb.coupon.backend.business.verification.context;

import com.fcb.coupon.backend.model.dto.CouponImportVerifyResultDto;
import com.fcb.coupon.common.excel.bean.RowParseResult;
import lombok.Data;

import java.util.Collection;
import java.util.TreeMap;

/**
 * @author YangHanBin
 * @date 2021-09-09 14:30
 */
@Data
public class BatchVerifyContext {
    /**
     * 核销人id
     */
    private Long verifyUserId;

    /**
     * 核销用户
     */
    private String verifyUsername;

    /**
     * 核销数据
     */
    private Collection<RowParseResult> rowParseResults;

    /**
     * 异步任务主键id
     */
    private Long asyncTaskId;

    /**
     * 核销结果map
     */
    private TreeMap<Integer, CouponImportVerifyResultDto> verifyResultMap;
}
