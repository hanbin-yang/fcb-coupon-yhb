package com.fcb.coupon.backend.remote.dto.out;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author HanBin_Yang
 * @since 2021/6/28 14:11
 */
@Data
@Accessors(chain = true)
public class AgencyInfoOutputDto {
    private String guid;
    private String brokerId;
    private String orgAccount;
    private String name;
    private String phone;
    private String orgId;
    private String unionId;

    private Long userId;
    /**
     * 是否禁用0:否 1:是
     */
    private Integer isDisabled;
}
