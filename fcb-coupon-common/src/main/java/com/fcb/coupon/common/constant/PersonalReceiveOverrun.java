package com.fcb.coupon.common.constant;

/**
 * 个人领券超限 枚举
 * @author YangHanBin
 * @date 2021-08-23 14:33
 */
public interface PersonalReceiveOverrun {
    // 超出个人总领券限制
    int OUT_TOTAL_LIMIT = -9999;
    // 超出每月领券限制
    int OUT_MONTH_LIMIT = -999;
    // 超出每天领券限制
    int OUT_DAY_LIMIT = -99;
}
