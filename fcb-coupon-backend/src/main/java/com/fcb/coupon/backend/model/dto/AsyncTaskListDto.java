package com.fcb.coupon.backend.model.dto;

import lombok.Data;

/**
 * 后台管理->营销中心->优惠券管理->券核销->券核销列表
 * @author mashiqiong
 * @date 2021-6-23 21:23
 */
@Data
public class AsyncTaskListDto {
    /**
     * 核销人Id
     */
    private Long createUserid;
    /**
     * 分页查询初始行
     */
    private Integer startItem;

    /**
     * 页面pageSize
     */
    private Integer itemsPerPage;

}
