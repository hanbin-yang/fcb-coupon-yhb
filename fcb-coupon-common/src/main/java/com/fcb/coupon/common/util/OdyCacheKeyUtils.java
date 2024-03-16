package com.fcb.coupon.common.util;

/**
 * 欧电云缓存key utils
 * @author YangHanBin
 * @date 2021-06-11 0:39
 */
@Deprecated
public class OdyCacheKeyUtils {
    public static final String COMPANY_ID = "180";
    public static final String FUNCTION_KEY = "_function";
    public static final String USER_KEY = "_user";
    public static final String UT_KEY = "UserTicket";
    private static final String USER_KEY_PREFIX = "{user}_";

    private static final String MERCHANT_AUTH_KEY = "_merchant";
    private static final String STORE_AUTH_KEY = "_store";
    public static final String UT_ERROR_MESSAGE_PREFIX = "ut_error_message_prefix_";
    private static final String CITY_AUTH_KEY = "_city";

    /**
     * 获取用户缓存key
     *
     * @param ut
     * @return
     */
    public static String getUserKey(String ut) {
        StringBuilder keyBuilder = new StringBuilder(USER_KEY_PREFIX);
        return keyBuilder.append(COMPANY_ID).append("_").append(ut).append(USER_KEY).append("_0").toString();
    }

    /**
     * 用户异常信息key
     * @param ut
     * @return
     */
    public static String getUserExceptionKey(String ut){
        return OdyCacheKeyUtils.USER_KEY_PREFIX+ OdyCacheKeyUtils.COMPANY_ID+"_"+ OdyCacheKeyUtils.UT_ERROR_MESSAGE_PREFIX + ut+"_0";
    }

    /**
     * 获取用户权限缓存key
     *
     * @param ut
     * @return
     */
    public static String getFunctionKey(String ut) {
        StringBuilder keyBuilder = new StringBuilder(USER_KEY_PREFIX);
        return keyBuilder.append(COMPANY_ID).append("_").append(ut).append(FUNCTION_KEY).append("_0").toString();
    }

    /**
     * 获取用户数据权限-集团、公司缓存key
     * @param userId 需要根据ut获取缓存中用户id，再查询
     * 示例：{user}_180_20041915014241_merchant_0
     */
    public static String getUserMerchantCacheKey(String userId) {
        StringBuilder keyBuilder = new StringBuilder(USER_KEY_PREFIX);
        return keyBuilder.append(COMPANY_ID).append("_").append(userId).append(MERCHANT_AUTH_KEY).append("_0").toString();
    }

    /**
     * 获取用户数据权限-楼盘缓存key
     *  @param userId 需要根据ut获取缓存中用户id，再查询
     * 示例：{user}_180_20041915014241_store_0
     */
    public static String getUserStoreCacheKey(String userId) {
        StringBuilder keyBuilder = new StringBuilder(USER_KEY_PREFIX);
        return keyBuilder.append(COMPANY_ID).append("_").append(userId).append(STORE_AUTH_KEY).append("_0").toString();
    }

    /**
     * 获取用户城市数据权限-城市缓存key
     *  @param ut 需要根据ut获取缓存中用户id，再查询
     * 示例：{user}_180_20041915014241_city_0
     */
    public static String getUserCityCacheKey(String ut) {
        StringBuilder keyBuilder = new StringBuilder(USER_KEY_PREFIX);
        return keyBuilder.append(COMPANY_ID).append("_").append(ut).append(CITY_AUTH_KEY).append("_0").toString();
    }
}
