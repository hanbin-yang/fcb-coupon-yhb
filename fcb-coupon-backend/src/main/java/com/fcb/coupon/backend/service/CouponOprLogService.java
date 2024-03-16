package com.fcb.coupon.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.backend.model.bo.CouponOprLogQueryBo;
import com.fcb.coupon.backend.model.dto.OprLogDo;
import com.fcb.coupon.backend.model.param.response.CouponOprLogResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.model.entity.CouponOprLogEntity;

import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-06-16 18:23
 */
public interface CouponOprLogService extends IService<CouponOprLogEntity> {
    /**
     * 异步日志
     * @param dto 入参
     */
    void saveOprLogAsync(OprLogDo dto);

    /**
     * 非异步
     * @param dto 入参
     */
    void saveOprLog(OprLogDo dto);

    void saveOprLogBatch(List<OprLogDo> doList);

    /**
     * 优惠券操作日志列表
     * @param bo
     * @return
     */
    PageResponse<CouponOprLogResponse> listByPageRequest(CouponOprLogQueryBo bo);
}
