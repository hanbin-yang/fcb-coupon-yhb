package com.fcb.coupon.app.remote.dto.output;

import lombok.Data;

/**
 * @author YangHanBin
 * @date 2021-08-17 10:49
 */
@Data
public class CustomerByUnionIdOutput {
    /**
     * 客户id
     */
    private String customerId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 手机号
     */
    private String mphone;
}
