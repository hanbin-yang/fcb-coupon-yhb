package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.backend.model.ao.DeleteOrgAo;
import lombok.Data;

import java.util.List;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 17:47
 */
@Data
public class MktUseRuleDeleteOrgBo {
    private Long userId;
    private String username;

    /**
     * 券活动主键
     */
    private Long couponThemeId;

    /**
     * mkt_use_rule表对应的组织范围  6:店铺 1:商家 11:集团
     */
    private Integer ruleType;

    /**
     * 要删除的组织的集合
     */
    private List<DeleteOrgAo> deleteOrgList;
}
