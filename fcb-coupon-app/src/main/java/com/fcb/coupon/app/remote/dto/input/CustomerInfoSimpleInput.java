package com.fcb.coupon.app.remote.dto.input;

import lombok.Data;

import java.util.List;

/**
 * 根据电话号码批量获取会员用户信息入参
 *
 * @Author WeiHaiQi
 * @Date 2021-06-21 22:55
 **/
@Data
public class CustomerInfoSimpleInput {

    /**
     * 手机号
     */
    private List<String> phoneNoList;
}
