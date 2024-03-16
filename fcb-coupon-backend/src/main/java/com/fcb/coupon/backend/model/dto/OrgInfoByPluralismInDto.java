package com.fcb.coupon.backend.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author yhb
 * @date 2021-04-02 15:33
 */

@Data
public class OrgInfoByPluralismInDto {
    /**
     * 级别
     * 集团ZB 商家FGS 店铺DP
     */
    private String orgLevelCode;
    /**
     * 组织名称集合
     */
    private List<String> orgNames;
    /**
     * 组织编码集合
     */
    private List<String> orgCodes;
    /**
     * 楼盘编码集合
     */
    private List<String> buildCodes;
}
