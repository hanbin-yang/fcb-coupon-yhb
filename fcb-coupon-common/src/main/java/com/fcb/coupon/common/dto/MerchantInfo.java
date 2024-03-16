package com.fcb.coupon.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-06-16 20:20
 */
@Data
public class MerchantInfo  implements Serializable {
    private static final long serialVersionUID = 3372406140109180951L;
    private List<AuthMerchantDTO> authMerchantList;
}
