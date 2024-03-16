package com.fcb.coupon.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-06-16 20:25
 */
@Data
public class StoreInfo implements Serializable {
    private List<AuthStoreDTO> authStoreList;
}
