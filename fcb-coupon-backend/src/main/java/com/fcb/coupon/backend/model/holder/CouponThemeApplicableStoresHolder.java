package com.fcb.coupon.backend.model.holder;

import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * @author HanBin_Yang
 * @since 2021/6/23 19:14
 */
@Data
public class CouponThemeApplicableStoresHolder {
    public static ThreadLocal<Map<Long, Set<Long>>> applicableStoresMap = new ThreadLocal<>();
}
