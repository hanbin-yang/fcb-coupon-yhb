package com.fcb.coupon.backend.model.dto;

import lombok.Data;

/**
 * @author HanBin_Yang
 * @since 2021/6/23 14:05
 */
@Data
public class StoreInfoOutDto {
    /**库存组织id */
    private Long storeId;
    /**库存组织名称 */
    private String storeName;
    /**库存组织编码 */
    private String storeCode;
    /* 楼盘编码 */
    private String buildCode;
    // B端上线状态，0未上线 1已上线
    private Integer buildOnlineStatus;
    // C端上线状态，0未上线 1已上线
    private Integer cpointBuildOnlineStatus;
    // 机构端上线状态，0未上线 1已上线
    private Integer orgPointBuildOnlineStatus;
}
