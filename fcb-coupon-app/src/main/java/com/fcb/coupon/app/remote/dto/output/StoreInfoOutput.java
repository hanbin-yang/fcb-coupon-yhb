package com.fcb.coupon.app.remote.dto.output;

import lombok.Data;

/**
 * @author YangHanBin
 * @date 2021-08-24 14:17
 */
@Data
public class StoreInfoOutput {
    /**
     * 楼盘编码
     */
    private String buildCode;
    /**
     * 楼盘id
     */
    private String storeId;
    /**
     * 楼盘名称
     */
    private String storeName;

    /**
     * B上线状态 0：未上线，1：已上线 null：位同步
     */
    private Integer buildOnlineStatus;

    /**
     * C端上线状态 0：未上线，1：已上线 null：位同步
     */
    private Integer cpointBuildOnlineStatus;

    /**
     * 机构端上线状态 0：未上线，1：已上线 null：位同步
     */
    private Integer orgPointBuildOnlineStatus;
}
