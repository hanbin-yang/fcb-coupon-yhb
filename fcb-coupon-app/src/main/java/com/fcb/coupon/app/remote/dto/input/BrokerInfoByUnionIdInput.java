package com.fcb.coupon.app.remote.dto.input;

import lombok.Data;

/**
 * 根据unionId获取B端用户信息--入参
 *
 * @Author WeiHaiQi
 * @Date 2021-08-17 20:53
 **/
@Data
public class BrokerInfoByUnionIdInput {

    private String unionId;
}
