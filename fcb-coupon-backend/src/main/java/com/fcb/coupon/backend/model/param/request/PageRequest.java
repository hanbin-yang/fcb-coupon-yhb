package com.fcb.coupon.backend.model.param.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 分页请求
 *
 * @Author Weihq
 * @Date 2021-06-15 16:00
 **/
@ApiModel(description = "分页请求类")
@Data
public class PageRequest<T> {

    @ApiModelProperty(value = "当前页码")
    private Integer currentPage;
    @ApiModelProperty(value = "页大小")
    private Integer itemsPerPage;
    @ApiModelProperty(value = "请求参数对象")
    private T obj;
}
