package com.fcb.coupon.common.enums;

import java.util.Objects;

/**
 * 券来源类型
 *
 * @author weihaiqi
 * @date 2021-06-16
 */
public enum CouponSourceTypeEnum {
    COUPON_SOURCE_NAMED_USER(1, "指定用户发放"),
    COUPON_SOURCE_REGISTER(2, "注册自动发放"),
    COUPON_SOURCE_ORDER(3, "交易完后发放"),
    COUPON_SOURCE_ACTIVITY(4, "主动领券"),

    COUPON_SOURCE_PRESENTED(5, "转赠"),
    COUPON_SOURCE_RED_PACKET(6, "红包"),
    COUPON_SOURCE_OFFLINE(7, "线下预制券"),
    COUPON_SOURCE_BIRTH(8, "生日券"),
    COUPON_SOURCE_FULL_COURT(9, "全场券"),
    COUPON_SOURCE_FIRST_LOGIN(10, "首次登录发放"),
    COUPON_SOURCE_LOTTERY(11, "抽奖券"),
    COUPON_SOURCE_MARKTEING_VOUCHER(17, "主动营销赠券"),
    COUPON_SOURCE_INTERESTS(18, "权益优惠券"),
    COUPON_SOURCE_THIRD_PARTY(21, "第三方导入"),
    COUPON_SOURCE_ACTIVY_RULE(22, "活动赠券"),
    COUPON_SOURCE_ASSIGN(23, "转让"),

    COUPON_SOURCE_MEDIA_ADVERT(24, "媒体广告领券"),
    COUPON_SOURCE_LIVE(25, "直播领券"),
    COUPON_SOURCE_MARKETING_ACTIVITY(26, "营销活动页领券"),
    COUPON_SOURCE_VIDEO_LIVE(27, "视频直播领券"),
    ;

    private Integer source;
    private String sourceStr;

    CouponSourceTypeEnum(Integer source, String sourceStr) {
        this.source = source;
        this.sourceStr = sourceStr;
    }

    public Integer getSource() {
        return source;
    }

    public String getSourceStr() {
        return sourceStr;
    }

    public static String getStrBySource(Integer source) {
        if (source != null) {
            for (CouponSourceTypeEnum couponThemeEnum : CouponSourceTypeEnum.values()) {
                if (Objects.equals(couponThemeEnum.getSource(), source)) {
                    return couponThemeEnum.getSourceStr();
                }
            }
        }
        return null;
    }

    /**
     * 根据source获取对应枚举
     * 不支持返回null
     * @param source
     * @return
     */
    public static CouponSourceTypeEnum sourceOf(Integer source) {
        if (source == null) {
            return null;
        }

        for (CouponSourceTypeEnum couponThemeEnum : CouponSourceTypeEnum.values()) {
            if (Objects.equals(couponThemeEnum.getSource(), source)) {
                return couponThemeEnum;
            }
        }

        return null;
    }

    /**
     * 检查是否与枚举type相同
     * @param source
     * @return
     */
    public Boolean ifSame(Integer source) {
        return null != source && this.source.equals(source);
    }
}
