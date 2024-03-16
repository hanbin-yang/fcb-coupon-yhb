package com.fcb.coupon.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author mashiqiong
 * @date 2021-8-20 21:53
 */
@Data
public class SaasUserLoginChectDto implements Serializable {
    private Integer tokenValid;
}
