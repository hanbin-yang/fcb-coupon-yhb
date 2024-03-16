package com.fcb.coupon.client.backend.param.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class CouponThemeListCmsRequest implements Serializable {

    @Valid
    @NotNull(message = "参数错误")
    private CouponThemeListCmsData data;

    @Data
    public static class CouponThemeListCmsData implements Serializable {

        @ApiModelProperty(value = "券活动Id列表")
        private List<Long> ids;

        @ApiModelProperty(value = "券活动Id列表")
        private Integer couponGiveRule;

        //0：B端 1：机构端/saas端 2：C端 (2021-03-24 v-1.7.0 修正)
        @ApiModelProperty(value = "用户类型")
        private Integer crowdScope;

        @ApiModelProperty(value = "当前页")
        private int currentPage;

        @ApiModelProperty(value = "每页多少条")
        private int itemsPerPage;

        @ApiModelProperty(value = "商家列表")
        private List<Long> merchantList;

        private Integer themeType;

        private Integer status;

        //是否过滤可领券数量为0的  0否  1：是；传入为1的情况，会查询审核通过未过期，且可领券数不为0的券活动
        private Integer limitFlag;
    }



}
