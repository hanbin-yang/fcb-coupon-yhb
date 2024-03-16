package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.backend.model.ao.AddOrgAo;
import lombok.Data;

import java.util.List;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 10:31
 */
@Data
public class CouponThemeAddOrgBo {
    private Long userId;
    private String username;
    private String ut;

    /**
     * mkt_use_rule表对应的组织范围  6:店铺 1:商家 11:集团
     */
    private Integer ruleType;
    /**
     * 券活动主键
     */
    private Long couponThemeId;

    /**
     * 添加组织的集合
     */
    private List<AddOrgAo> orgAddList;
}
