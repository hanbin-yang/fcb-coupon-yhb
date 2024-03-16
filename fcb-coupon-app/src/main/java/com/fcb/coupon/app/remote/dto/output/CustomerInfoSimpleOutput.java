package com.fcb.coupon.app.remote.dto.output;

import lombok.Data;

import java.io.Serializable;

@Data
public class CustomerInfoSimpleOutput implements Serializable {

    /**
     * 客户id
     */
    private String customerId;
    /**
     * 统一用户ID
     */
    private String unionId;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 手机号
     */
    private String phoneNo;

    /**
     * 认证时间
     */
    private String applyTime;
}
