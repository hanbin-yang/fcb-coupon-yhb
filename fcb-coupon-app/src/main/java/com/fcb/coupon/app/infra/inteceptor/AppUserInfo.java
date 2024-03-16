package com.fcb.coupon.app.infra.inteceptor;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author YangHanBin
 * @date 2021-08-19 11:23
 */
@Data
@Accessors(chain = true)
public class AppUserInfo {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户手机号
     */
    private String userMobile;
    /**
     * 用户昵称
     */
    private String nickName;
    /**
     * 用户类型，0：B端  1：SAAS端  2：C端
     */
    private Integer userType;

    /**
     * 用户unionId
     */
    private String unionId;
}
