package com.fcb.coupon.backend.model.bo;

import lombok.Data;

import java.util.List;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 20:43
 */
@Data
public class MktUseRuleImportAddOrgBo {
    private Long userId;
    private String username;
    private String ut;

    /**
     * 券活动主键
     */
    private Long couponThemeId;

    /**
     * mkt_use_rule表对应的组织范围  6:店铺 1:商家 11:集团
     */
    private Integer ruleType;
    /**
     * 导入的数据
     */
    List<AddOrgImportBo> importDataList;
}
