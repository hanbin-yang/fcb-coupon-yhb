package com.fcb.coupon.backend.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

@Data
public class CouponThemeCmsPageQuery<CouponThemeEntity> extends Page<CouponThemeEntity> {

    public CouponThemeCmsPageQuery(long current, long size) {
        super(current, size);
    }

    private List<Long> ids;

    private Integer couponGiveRule;

    private Integer crowdScope;

    private int currentPage;

    private int itemsPerPage;

    private List<Long> merchantList;

    private Integer themeType;

    private Integer status;

    //是否过滤可领券数量为0的  0否  1：是；
    private Integer limitFlag;

}
