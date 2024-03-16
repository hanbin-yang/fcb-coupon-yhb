package com.fcb.coupon.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author YangHanBin
 * @date 2021-06-16 20:21
 */
@Data
public class ChannelInfoOutDTO implements Serializable {
    private static final long serialVersionUID = -4220055860978227339L;

    private String channelCode;
    private String channelName;
    private String channelMode;
    private Long orgId;
}
