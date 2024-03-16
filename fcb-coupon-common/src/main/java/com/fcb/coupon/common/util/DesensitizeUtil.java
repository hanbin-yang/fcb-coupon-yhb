package com.fcb.coupon.common.util;

import org.apache.commons.lang3.StringUtils;


/**
 * 脱敏工具类
 * @author YangHanBin
 * @date 2021-07-06 19:15
 */
public class DesensitizeUtil {

    public static String around(String str, int startOffSet, int endOffset) {
        return replace(str, startOffSet, endOffset, '*');
    }

    public static String replace(String str, int startOffSet, int endOffset, char replacedChar) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        final int length = str.length();
        final char[] cs = new char[length];

        if (startOffSet >= length || endOffset >= length) {
            for (int i = 0; i < length; i++) {
                cs[i] = replacedChar;
            }
            return new String(cs);
        }

        if (endOffset < 0) {
            endOffset = 0;
        }

        if (endOffset >= length - startOffSet) {
            endOffset = length - startOffSet - 1;
        }

        for (int i = 0; i < length; i++) {
            if (i >= startOffSet && i < length - endOffset) {
                cs[i] = replacedChar;
            } else {
                cs[i] = str.charAt(i);
            }
        }
        return new String(cs);
    }
}
