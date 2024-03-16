package com.fcb.coupon.app.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月27日 11:56:00
 */
@Data
public class PageDto {

    @ApiModelProperty(value = "当前页码")
    private Integer current;

    @ApiModelProperty(value = "页记录数")
    private Integer page;

    @ApiModelProperty(value = "开始项")
    private Integer start;

    public PageDto(Integer current, Integer page) {
        this.current = current;
        this.page = page;
        this.start = (current - 1) * page;
    }

}
