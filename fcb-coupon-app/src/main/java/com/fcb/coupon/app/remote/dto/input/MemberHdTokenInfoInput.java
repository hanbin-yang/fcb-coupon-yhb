package com.fcb.coupon.app.remote.dto.input;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author YangHanBin
 * @date 2021-08-18 11:51
 */
@Data
public class MemberHdTokenInfoInput {
    @ApiModelProperty(value = "登录ut")
    private String hdToken;

    @ApiModelProperty(value = "终端类型 ios miniapp")
    private String terminalType;
}
