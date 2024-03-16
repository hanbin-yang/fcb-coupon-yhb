package com.fcb.coupon.common.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 字符串脱敏工具类
 *
 * @Author WeiHaiQi
 * @Date 2021-06-17 19:35
 **/
public class DesensitizationUtil {

    /**
     * 券码脱敏
     * 规则：1位的时候，直接显示，2位第二位不显示，3位到8位除第1位和最后一位均打星，8位以上除前四和后四都打星
     * @param couponCode
     * @return
     */
    public static String maskCouponCode(String couponCode) {
        if(StringUtils.isBlank(couponCode)){
            return "";
        }
        if (couponCode.length() == 1) {
            return couponCode;
        } else  if (couponCode.length() == 2) {
            StringBuilder stringBuilder = new StringBuilder(couponCode);
            stringBuilder.replace(1, 2, "*");
            return stringBuilder.toString();
        } else if (couponCode.length() > 2 && couponCode.length() <= 8) {
            char[] chars = couponCode.toCharArray();
            StringBuffer sb = new StringBuffer();
            int begin = 1;
            int end = couponCode.length()-2;
            for (int i=0;i<chars.length;i++){
                char c = chars[i];
                if(i>=begin&&i<=end){
                    c = '*';
                }
                sb.append(c);
            }
            return sb.toString();
        } else if(couponCode.length()>8){
            char[] chars = couponCode.toCharArray();
            StringBuffer sb = new StringBuffer();
            int begin = 4;
            int end = couponCode.length()-5;
            for (int i=0;i<chars.length;i++){
                char c = chars[i];
                if(i>=begin&&i<=end){
                    c = '*';
                }
                sb.append(c);
            }
            return sb.toString();
        }
        return couponCode;
    }

    /**
     * 电话号码脱敏
     * @param cellNo
     * @return
     */
    public static String maskCouponcell(String cellNo) {
        if(StringUtils.isBlank(cellNo)){
            return "";
        }
        if(cellNo.length() >= 6) {
            char[] chars = cellNo.toCharArray();
            StringBuffer sb = new StringBuffer();
            int begin = cellNo.length()/2-2;
            int end =  cellNo.length()/2+1;
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (i >= begin && i <= end) {
                    c = '*';
                }
                sb.append(c);
            }
            return sb.toString();
        }
        return cellNo;
    }
}
