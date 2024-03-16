package com.fcb.coupon.backend.model.dto;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

/**
 * @author HanBin_Yang
 * @since 2021/6/25 14:21
 */
@Data
public class VerifyUserInfoDto {
    /**
     * 核销用户id
     */
    private String verifyUserId;
    /**
     * 关联ID,埋点要用
     */
    private String verifyUnionId;
    /**
     * 核销手机号
     */
    private String verifyPhone;
    /**
     * 是否禁用0:否 1:是
     */
    private Integer isDisabled;
    /**
     * 核销用户类型
     */
    private Integer userType;
    /**
     * 核销券绑定的userId
     */
    private String dbUserId;
    /**
     * 券绑定的手机号
     */
    private String dbBindTel;
    /**
     * 核销券主题适用人群
     */
    private JSONArray applicableUserTypes;
    /**
     * 核销标志 0单个核销 1批量核销
     */
    private Integer verifyFlag;
}
