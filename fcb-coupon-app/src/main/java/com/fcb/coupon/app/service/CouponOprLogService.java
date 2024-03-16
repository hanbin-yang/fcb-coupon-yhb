package com.fcb.coupon.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.app.model.dto.OprLogDo;
import com.fcb.coupon.app.model.entity.CouponOprLogEntity;

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

}
