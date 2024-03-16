package com.fcb.coupon.common.constant;

/**
 * redis锁 keyName定义 String
 *
 * @author YangHanBin
 * @date 2021-06-11 16:46
 */
public class RedisCacheKeyConstant {
    /**
     * 优惠券项目 key前缀
     */
    private static final String COUPON_CACHE_KEY_PREFIX = "promotion:cache:";
    /**
     * couponTheme相关key前缀
     */
    private static final String COUPON_THEME_PREFIX = "couponTheme:";
    /**
     * coupon相关key前缀
     */
    private static final String COUPON_PREFIX = "coupon:";
    /**
     * coupon相关key前缀
     */
    private static final String COUPON_USER_STATISTIC = "couponUserStatistic:";
    /**
     * couponTheme 数据库缓存
     */
    public static final String COUPON_THEME_DATABASE = COUPON_CACHE_KEY_PREFIX + COUPON_THEME_PREFIX + "id:exist:";
    /**
     * 数据库不存在的 couponThemeId
     */
    public static final String NON_EXIST_COUPON_THEME_ID = COUPON_CACHE_KEY_PREFIX + COUPON_THEME_PREFIX + "id:notExist:";
    /**
     * 第三方券码缓存list
     */
    public static final String COUPON_THIRD_COUPON_LIST = COUPON_CACHE_KEY_PREFIX + COUPON_PREFIX + "couponid:list:";
    /**
     * coupon表数据刷新到elasticsearch
     */
    public static final String COUPON_REFRESH_TO_ES = COUPON_CACHE_KEY_PREFIX + COUPON_THEME_PREFIX + "coupon:refreshToEs:couponThemeId:";

    /**
     * 转赠前记录
     */
    public static final String COUPON_BEFORE_GIVE = COUPON_CACHE_KEY_PREFIX + COUPON_PREFIX + "couponBeforeGive:id:";
    /**
     * 个人领券情况信息
     */
    public static final String COUPON_USER_STATISTIC_DB = COUPON_CACHE_KEY_PREFIX + COUPON_USER_STATISTIC + "couponThemeId:{0}:userId:{1}:userType:{2}";

    /**
     * 验证码超时
     */
    public static final String MOBILE_CAPTCHAS_EXPIRE = "mobile_captchas_expire_%s_%s_%s";

    /**
     * 验证码错误次数
     */
    public static final String MOBILE_CAPTCHAS_FAIL = "mobile_captchas_fail_%s_%s_%s";
    /**
     * 根据unionId获取B/C端用户信息缓存
     */
    public static final String UNION_USERINFO_CACHE = COUPON_CACHE_KEY_PREFIX + COUPON_PREFIX + "userInfo:union:{0}:userType:{1}";
    /**
     * 根据手机号获取B/C端用户信息缓存
     */
    public static final String PHONE_USERINFO_CACHE = COUPON_CACHE_KEY_PREFIX + COUPON_PREFIX + "userInfo:phone:{0}:userType:{1}";
}
