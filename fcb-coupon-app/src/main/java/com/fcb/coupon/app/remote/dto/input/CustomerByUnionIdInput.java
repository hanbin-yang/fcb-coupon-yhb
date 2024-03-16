package com.fcb.coupon.app.remote.dto.input;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 根据unionId获取C端用户信息--入参
 * @author YangHanBin
 * @date 2021-08-17 10:48
 */
@Data
@Accessors(chain = true)
public class CustomerByUnionIdInput {
    private String unionId;
}
