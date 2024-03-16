package com.fcb.coupon.backend.model.param.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 分页响应返回类
 *
 * @Author Weihq
 * @Date 2021-06-15 18:02
 **/
@ApiModel(value="分页响应返回类",description="分页响应返回类")
@Data
public class PageResponse<T> {

    @ApiModelProperty(value="总条数")
    private int total;

    @ApiModelProperty(value="查询结果集")
    private List<T> listObj;

    public PageResponse(List<T> listObj, int total) {
        super();
        this.listObj = listObj;
        this.total = total;
    }

    public PageResponse() {
    }
}
