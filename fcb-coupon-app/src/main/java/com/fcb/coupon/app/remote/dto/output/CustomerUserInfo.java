package com.fcb.coupon.app.remote.dto.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author YangHanBin
 * @date 2021-08-18 11:39
 */
@Data
public class CustomerUserInfo {
    /**
     * C端用户id
     */
    @JsonProperty(value = "customerId")
    private String userId;
    /**
     * 用户手机号
     */
    private String phone;
    /**
     * 用户名称
     */
    private String name;
    /**
     * 用户昵称
     */
    private String nickName;
    /**
     * 统一用户id
     */
    private String unionId;

    private Boolean loginFlag;
}
