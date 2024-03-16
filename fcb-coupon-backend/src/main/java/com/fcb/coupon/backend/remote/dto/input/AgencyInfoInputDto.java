package com.fcb.coupon.backend.remote.dto.input;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author HanBin_Yang
 * @since 2021/6/28 14:08
 */
@Data
@Accessors(chain = true)
public class AgencyInfoInputDto {
    private List<String> orgAccounts;
}
