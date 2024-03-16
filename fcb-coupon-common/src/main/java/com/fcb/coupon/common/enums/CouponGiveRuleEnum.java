package com.fcb.coupon.common.enums;


import java.util.stream.Stream;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月09日 18:59:00
 */
public enum CouponGiveRuleEnum {

    COUPON_GIVE_RULE_ACTIVY_RULE(1, "活动规则券"),
    COUPON_GIVE_RULE_FRONT(4, "前台领券"),
    COUPON_GIVE_RULE_MARKTEING_VOUCHER(17, "主动营销券"),
    COUPON_GIVE_RULE_INTERESTS(18, "权益优惠券"),
    COUPON_GIVE_RULE_OFFLINE_PREFABRICATED(19, "线下预制券"),
    COUPON_GIVE_RULE_MEDIA_ADVERT(20, "媒体广告券"),
    COUPON_GIVE_RULE_LIVE(21, "直播券"),
    COUPON_GIVE_RULE_MARKETING_ACTIVITY(22, "营销活动页券"),
    ;
    private Integer type;
    private String typeStr;

    CouponGiveRuleEnum(Integer type, String typeStr) {
        this.type = type;
        this.typeStr = typeStr;
    }

    public Integer getType() {
        return type;
    }

    public String getTypeStr() {
        return typeStr;
    }

    public static String getStrByType(Integer type) {
        if (type != null) {
            for (CouponGiveRuleEnum couponGiveRule : CouponGiveRuleEnum.values()) {
                if (couponGiveRule.type.equals(type)) {
                    return couponGiveRule.getTypeStr();
                }
            }
        }
        return null;
    }

    /**
     * 检查是否与枚举type相同
     * @param type
     * @return
     */
    public Boolean ifSame(Integer type) {
        return null != type && this.type.equals(type);
    }

    public static CouponGiveRuleEnum of(Integer type) {

        return Stream.of(values())
                .filter(bean -> bean.type.equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("CouponGiveRuleEnum: [type=" + type + "]不存在！"))
                ;
    }

}
