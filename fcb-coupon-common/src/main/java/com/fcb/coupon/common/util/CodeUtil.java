package com.fcb.coupon.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * @author YangHanBin
 * @date 2021-06-18 14:49
 */
public class CodeUtil {


    private static final Logger log = LoggerFactory.getLogger(CodeUtil.class);

    public static String generateCouponCode() {
        String uid = (UUID.randomUUID().toString()).replace("-", "");
        try {
            return MD5.code(uid, 16);
        } catch (Exception e) {
            log.error("generateCouponCode fail: message={}", e.getMessage(), e);
            throw new RuntimeException();
        }
    }
}
