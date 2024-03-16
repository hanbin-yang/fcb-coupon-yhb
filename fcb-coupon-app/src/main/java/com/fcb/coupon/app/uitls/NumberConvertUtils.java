package com.fcb.coupon.app.uitls;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 唐陆军
 * @Description 数字转换
 * @createTime 2021年07月06日 17:52:00
 */
public class NumberConvertUtils {

    public static final char[] array = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public static Map<Character, Integer> charValueMap = new HashMap<Character, Integer>();

    //初始化map
    static {
        for (int i = 0; i < array.length; i++) {
            charValueMap.put(array[i], i);
        }
    }


    /**
     * 把数字转换成相对应的69进制
     *
     * @param number
     * @return
     */
    public static String numberConvertToShortStr(long number) {
        int decimal = array.length;
        StringBuilder builder = new StringBuilder();
        while (number != 0) {
            builder.append(array[(int) (number - (number / decimal) * decimal)]);
            number /= decimal;
        }
        return builder.reverse().toString();
    }

    /**
     * 把进制字符串转换成相应的数字
     */
    public static long shortStrConvertToNumber(String shortStr) throws NumberFormatException {
        int decimal = array.length;
        long sum = 0;
        long multiple = 1;
        char[] chars = shortStr.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            char c = chars[i];
            Integer value = charValueMap.get(c);
            if (value == null) {
                throw new NumberFormatException(shortStr + "中 " + c + " 是不合法的短字符");
            }
            sum += value * multiple;
            multiple *= decimal;
        }
        return sum;
    }
}
