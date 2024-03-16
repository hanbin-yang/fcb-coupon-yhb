package com.fcb.coupon.common.constant;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

/**
 * redis缓存 keyName定义 String
 *
 * @author YangHanBin
 * @date 2021-06-11 16:46
 */
public class RedisLockKeyConstant {
    /**
     * 优惠券项目 key前缀
     */
    private static final String COUPON_LOCK_KEY_PREFIX = "promotion:lock:";

    private static final String COUPON_PREFIX = "coupon:";

    /**
     * couponTheme相关key前缀
     */
    private static final String COUPON_THEME_PREFIX = "couponTheme:";
    /**
     * couponUserStatistic相关key前缀
     */
    private static final String COUPON_USER_STATISTIC_PREFIX = "couponUserStatistic:";
    /**
     * mkt_use_rule表相关key前缀
     */
    private static final String MKT_USE_RULE = "mkt_use_rule:";

    private static final String COUPON_VERIFICATION = "couponVerification:";


    /**
     * 同步coupon_theme数据库到couponThemeCache 锁
     */
    public static final String SYNC_COUPON_THEME_DATABASE_TO_CACHE = COUPON_LOCK_KEY_PREFIX + COUPON_THEME_PREFIX + "sync:themeId:";
    /**
     * 同步coupon_user_statistic数据库到couponUserStatisticCache 锁
     */
    public static final String SYNC_COUPON_USER_STATISTIC_DB_TO_CACHE = COUPON_LOCK_KEY_PREFIX + COUPON_USER_STATISTIC_PREFIX + "sync:couponThemeId:{0}:userId:{1}:userType:{2}";

    public static final String OPERATE_COUPON_THEME = COUPON_LOCK_KEY_PREFIX + COUPON_THEME_PREFIX + "opr:themeId:";

    public static final String OPERATE_MKT_USE_RULE = COUPON_LOCK_KEY_PREFIX + MKT_USE_RULE + "opr:themeId:";

    /**
     * 第三方优惠券导入锁
     */
    public static final String COUPON_THIRD_IMPORT_LOCK_KEY = COUPON_LOCK_KEY_PREFIX + "import:third:{0}";
    /**
     * 用户领券锁
     */
    public static final String COUPON_RECEIVE_LOCK_KEY = COUPON_LOCK_KEY_PREFIX + "receive:coupon:{0}:{1}";
    /**
     * 用户转赠领券锁
     */
    public static final String COUPON_GIVE_LOCK_KEY = COUPON_LOCK_KEY_PREFIX + "give:coupon:{0}:{1}";

    /**
     * 操作核销表
     */
    public static final String SINGLE_COUPON_VERIFICATION = COUPON_LOCK_KEY_PREFIX + COUPON_VERIFICATION + "verify:couponCode:";

    /**
     * 操作优惠券 上锁/解锁/换绑/核销
     */
    public static final String COUPON_OPERATION = COUPON_LOCK_KEY_PREFIX + COUPON_PREFIX + "opr:mingYuan:tsId:";

    public static String getThirdImportLockKey(Long themeId) {
        return MessageFormat.format(COUPON_THIRD_IMPORT_LOCK_KEY, themeId.toString());
    }

    public static String getCouponReceiveLockKey(Long themeId, String userId) {
        return MessageFormat.format(COUPON_RECEIVE_LOCK_KEY, themeId.toString(), userId);
    }

    public static String getCouponGiveLockKey(Long beforeGiveId, String userId) {
        return MessageFormat.format(COUPON_GIVE_LOCK_KEY, beforeGiveId.toString(), userId);
    }
}
