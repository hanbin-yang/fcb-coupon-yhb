package com.fcb.coupon.app.remote.dto;

import lombok.Data;

/**
 * 根据unionId查询C端用户信息DTO
 *
 * @Author WeiHaiQi
 * @Date 2021-08-16 17:40
 **/
@Data
public class CustomerByUnionIdDto {

    /**
     * 客户id
     */
    private String customerId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 手机号
     */
    private String mphone;
}
