package com.fcb.coupon.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.backend.model.bo.BatchVerifyBo;
import com.fcb.coupon.backend.model.bo.CouponSingleVerifyBo;
import com.fcb.coupon.backend.model.bo.CouponVerificationListBo;
import com.fcb.coupon.backend.model.dto.CouponVerificationStatisticDo;
import com.fcb.coupon.backend.model.entity.CouponVerificationEntity;
import com.fcb.coupon.backend.model.param.response.CouponVerificationDetailResponse;
import com.fcb.coupon.backend.model.param.response.CouponVerificationExportResponse;
import com.fcb.coupon.backend.model.param.response.CouponVerificationListResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;

import java.util.List;

/**
 * @author HanBin_Yang
 * @since 2021/6/23 9:16
 */
public interface CouponVerificationService extends IService<CouponVerificationEntity> {
    void couponVerifyWithTx(CouponVerificationEntity entity, Integer oldCouponVersionNo, Boolean isOfflineCoupon);

    PageResponse<CouponVerificationListResponse> list(CouponVerificationListBo bo);

    PageResponse<CouponVerificationExportResponse> export(CouponVerificationListBo bo);

    Long exportCouponVerificationListAsync(CouponVerificationListBo bo);

    int conditionalCount(CouponVerificationListBo bo);

    CouponVerificationDetailResponse getDetailById(Long id);

    /**
     * 统计核销数
     * @param couponThemeIds
     * @return
     */
    List<CouponVerificationStatisticDo> listVerificationCount(List<Long> couponThemeIds);
}
