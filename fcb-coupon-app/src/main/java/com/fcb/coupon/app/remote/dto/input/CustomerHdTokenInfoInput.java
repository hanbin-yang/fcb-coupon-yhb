package com.fcb.coupon.app.remote.dto.input;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author YangHanBin
 * @date 2021-08-18 11:36
 */
@Data
public class CustomerHdTokenInfoInput {
    @ApiModelProperty(value = "身份认证的令牌")
    private String hdToken;

    @ApiModelProperty(value = "终端类型: android/ios/web/miniapp")
    private String terminalType;
}
