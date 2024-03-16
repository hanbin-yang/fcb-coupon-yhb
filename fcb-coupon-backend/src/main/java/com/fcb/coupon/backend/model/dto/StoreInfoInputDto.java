package com.fcb.coupon.backend.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author HanBin_Yang
 * @since 2021/6/23 14:06
 */
@Data
public class StoreInfoInputDto {
    private List<Long> storeIds;

    // 楼盘编码集合
    private List<String> buildCodes;
}
