package com.fcb.coupon.common.util;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MobileValidateUtil {

    //public static final Pattern PATTERN = Pattern.compile("^[1]\\d{10}$");
    public static final Pattern PATTERN = Pattern.compile("^((222)|(1[3-9][0-9]))\\d{8}$");
    /**
     * 手机号验证
     *
     * @param  str
     * @return 验证通过返回true
     */
    public static boolean isMobile(String str) {
        if(!StringUtils.hasText(str)) {
            return false;
        }
        Matcher m = PATTERN.matcher(str);
        return m.matches();
    }


    /**
     * 验证是否能转换Integer
     *
     * @param  str
     * @return 验证通过返回true
     */
    public static boolean isInteger(String str) {
        boolean b;
        if(!StringUtils.hasText(str)) {
            return false;
        }
        try {
            Integer.parseInt(str);
            b = true;
        }catch (Exception e){
            b = false;
        }
        return b;
    }
}
