package com.fcb.coupon.app.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户中心用户信息
 *
 * @Author WeiHaiQi
 * @Date 2021-08-16 17:13
 **/
@Data
public class RealUserInfoDto implements Serializable {

    private static final long serialVersionUID = 436798684433272511L;

    /**
     * 用户ID
     */
    private String realUserId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 电话
     */
    private String mobilePhone;

    private String realNameAccrodingToAuth;
}
