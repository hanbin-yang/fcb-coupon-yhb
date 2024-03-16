package com.fcb.coupon.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fcb.coupon.backend.model.dto.AsyncTaskListDto;
import com.fcb.coupon.backend.model.entity.AsyncTaskEntity;

import java.util.List;

/**
 * @author HanBin_Yang
 * @since 2021/6/24 14:08
 */
public interface AsyncTaskMapper extends BaseMapper<AsyncTaskEntity> {
    /**
     * 查询导入核销任务列表
     * @param dto 查询条件
     * @return
     */
    List<AsyncTaskEntity> listAsyncTask(AsyncTaskListDto dto);
    /**
     * 查询导入核销任务列表 统计数量
     * @param dto 查询条件
     * @return
     */
    Integer listAsyncTaskCount(AsyncTaskListDto dto);
}
