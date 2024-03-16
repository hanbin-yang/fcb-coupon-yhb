package com.fcb.coupon.backend.model.dto;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

/**
 * @author HanBin_Yang
 * @since 2021/6/23 14:23
 */
@Data
public class ValidateStoreInfoDto {
    /**
     * 活动类型 0：平台券  11：商家券 5：集团券 21：店铺券
     */
    private Integer themeType;
    /**
     * 券活动id
     */
    private Long couponThemeId;

    private JSONArray couponThemePubPorts;

    private Integer userType;

    /**库存组织id */
    private Long storeId;

    // B端上线状态，0未上线 1已上线
    private Integer buildOnlineStatus;
    // C端上线状态，0未上线 1已上线
    private Integer cpointBuildOnlineStatus;
    // 机构端上线状态，0未上线 1已上线
    private Integer orgPointBuildOnlineStatus;
}
