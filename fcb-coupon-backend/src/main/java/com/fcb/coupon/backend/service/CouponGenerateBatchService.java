package com.fcb.coupon.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.backend.model.entity.CouponGenerateBatchEntity;
import com.fcb.coupon.backend.model.param.request.PageRequest;
import com.fcb.coupon.backend.model.param.response.CouponGenerateBatchResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;

public interface CouponGenerateBatchService extends IService<CouponGenerateBatchEntity> {

    PageResponse<CouponGenerateBatchResponse> listPage(PageRequest request);

//    /**
//     * 查询发券批次导入任务
//     * @param couponGenerateBatchId
//     * @return
//     */
//    ExportImportCouponTaskDto queryCouponGenerateLog(Long couponGenerateBatchId);

}
