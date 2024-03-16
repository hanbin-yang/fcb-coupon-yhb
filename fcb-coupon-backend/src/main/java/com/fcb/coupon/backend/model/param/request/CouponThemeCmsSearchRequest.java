package com.fcb.coupon.backend.model.param.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 唐陆军
 * @Description cms查询优惠券活动列表参数
 * @createTime 2021年09月02日 18:06:00
 */
@Data
public class CouponThemeCmsSearchRequest implements Serializable {


    private Long id;

    private String themeTitle;

    private Integer themeType;

    private Integer couponGiveRule;

    private Integer status;

    private Integer crowdScope;

    private List<String> receiveChannelCodes;

    //是否过滤可领券数量为0的 (0否,1：传入为1的情况，会查询审核通过未过期，且可领券数不为0的券活动)
    private Integer limitFlag;

    private List<Long> merchantList;

    private Integer currentPage;

    private Integer itemsPerPage;
}
