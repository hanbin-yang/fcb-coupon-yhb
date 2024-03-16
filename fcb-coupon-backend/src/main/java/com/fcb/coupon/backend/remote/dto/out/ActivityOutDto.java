package com.fcb.coupon.backend.remote.dto.out;

import lombok.Data;

import java.util.List;

/**
 * 活动详情
 *
 * @Author WeiHaiQi
 * @Date 2021-06-21 23:07
 **/
@Data
public class ActivityOutDto {

    /**
     * 活动id
     */
    private Long id;
    /**
     * 活动奖励相关优惠券id
     */
    private List<String> couponIds;
}
