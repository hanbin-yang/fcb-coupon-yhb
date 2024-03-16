package com.fcb.coupon.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.backend.model.bo.AsyncTaskListBo;
import com.fcb.coupon.backend.model.param.response.AsyncTaskListResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.model.entity.AsyncTaskEntity;

/**
 * @author HanBin_Yang
 * @since 2021/6/24 14:09
 */
public interface AsyncTaskService extends IService<AsyncTaskEntity> {
    PageResponse<AsyncTaskListResponse> list(AsyncTaskListBo bo);
}
