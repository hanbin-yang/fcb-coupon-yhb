package com.fcb.coupon.backend.model.dto;

import lombok.Data;

/**
 * @author yhb
 * @date 2021-04-02 15:42
 */
@Data
public class OrgInfoByPluralismOutDto {
    /**
     * 组织id
     */
    private Long orgId;
    /**
     * 组织名称
     */
    private String orgName;
    /**
     * 组织代码
     */
    private String orgCode;
    /**
     * 级别为楼盘时的楼盘编码
     */
    private String buildCode;
}
