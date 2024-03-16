package com.fcb.coupon.backend.model.bo;

import lombok.Data;

/**
 * @author HanBin_Yang
 * @since 2021/6/22 15:04
 */
@Data
public class AddOrgImportBo {
    /**
     * 组织编码
     */
    private String orgCode;
    /**
     * 组织名称
     */
    private String orgName;
}
