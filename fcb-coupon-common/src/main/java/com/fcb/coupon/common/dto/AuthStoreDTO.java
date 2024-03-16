package com.fcb.coupon.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-06-16 20:26
 */
@Data
public class AuthStoreDTO implements Serializable {
    private Long storeId;
    private String storeCode;
    private String storeName;
    private Long merchantId;
    private String merchantCode;
    private String merchantName;
    private List<ChannelInfoOutDTO> channelInfoList;
    private List<String> channelCodes;
    private List<Long> merchantIds;
    private String groupName;
    private Long groupId;
    private String groupCode;
    private String projectGuid;
    private String buildCode;
    private Integer buildOnlineStatus;
    private Integer cpointBuildOnlineStatus;
    private Integer orgPointBuildOnlineStatus;
}
