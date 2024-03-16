package com.fcb.coupon.backend.model.bo;

import lombok.Data;

/**
 * @author mashiqiong
 * @date 2021-6-23 16:31
 */
@Data
public class MktUseRuleCouponThemeDetailBo {
    /**
     * 券活动id
     */
    private Long themeRef;
    /**
     * 规则类型：0：券规则；1：卡规则；2：促销规则
     */
    private Integer refType;
}
