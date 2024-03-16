package com.fcb.coupon.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.mapper.AsyncTaskMapper;
import com.fcb.coupon.backend.model.bo.AsyncTaskListBo;
import com.fcb.coupon.backend.model.dto.AsyncTaskListDto;
import com.fcb.coupon.backend.model.param.response.AsyncTaskListResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.service.AsyncTaskService;
import com.fcb.coupon.backend.model.entity.AsyncTaskEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HanBin_Yang
 * @since 2021/6/24 14:09
 */
@Service
public class AsyncTaskServiceImpl extends ServiceImpl<AsyncTaskMapper, AsyncTaskEntity> implements AsyncTaskService {
    @Override
    public PageResponse<AsyncTaskListResponse> list(AsyncTaskListBo bo) {

        AsyncTaskListDto dto = new AsyncTaskListDto();
        dto.setItemsPerPage(bo.getItemsPerPage());
        dto.setStartItem(bo.getStartItem());
        dto.setCreateUserid(bo.getUserId());
        int total = this.baseMapper.listAsyncTaskCount(dto);

        PageResponse<AsyncTaskListResponse> pageResponse = new PageResponse<>();
        List<AsyncTaskListResponse> listObj = new ArrayList<>();
        pageResponse.setTotal(total);
        pageResponse.setListObj(listObj);

        if(total == 0){
            return pageResponse;
        }

        List<AsyncTaskEntity> asyncTaskEntityList = this.baseMapper.listAsyncTask(dto);
        if (CollectionUtils.isEmpty(asyncTaskEntityList)){
            return pageResponse;
        }

        for (AsyncTaskEntity entity : asyncTaskEntityList) {
            AsyncTaskListResponse asyncTaskListResponse = new AsyncTaskListResponse();
            BeanUtil.copyProperties(entity, asyncTaskListResponse);
            listObj.add(asyncTaskListResponse);
        }
        return pageResponse;
    }
}
