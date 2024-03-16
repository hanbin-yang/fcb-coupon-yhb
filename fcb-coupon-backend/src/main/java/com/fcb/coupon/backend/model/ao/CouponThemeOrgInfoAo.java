package com.fcb.coupon.backend.model.ao;

import lombok.Data;

import java.io.Serializable;

/**
 * @author mashiqiong
 * @date 2021-6-22 15:46
 */
@Data
public class CouponThemeOrgInfoAo implements Serializable {
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
}
