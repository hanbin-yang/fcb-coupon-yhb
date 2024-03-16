package com.fcb.coupon.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-06-16 20:20
 */
@Data
public class AuthMerchantDTO implements Serializable {
    private static final long serialVersionUID = 3372406140109180901L;

    private Long merchantId;
    private String merchantCode;
    private String merchantName;
    private Integer merchantType;
    private List<ChannelInfoOutDTO> channelInfoList;
    private List<String> channelCodes;
    private String orgLevelCode;
    private Integer level;
    private String parentCode;
    private Long parentId;
    private String parentName;
    private String parentOrgLevelCode;

}
