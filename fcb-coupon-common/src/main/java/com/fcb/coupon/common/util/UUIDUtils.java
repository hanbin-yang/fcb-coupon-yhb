package com.fcb.coupon.common.util;

import java.util.UUID;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月17日 17:25:00
 */
public class UUIDUtils {

    public static String getShortUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
