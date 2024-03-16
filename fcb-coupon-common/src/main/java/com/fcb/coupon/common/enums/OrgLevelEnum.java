package com.fcb.coupon.common.enums;

import java.util.stream.Stream;

/**
 * @author HanBin_Yang
 * @since 2021/6/23 12:11
 */
public enum OrgLevelEnum {
    PLATFORM(0, 0,0, "平台", "PT"),
    GROUP(1, 5, 11, "集团", "ZB"),
    MERCHANT(2, 11, 1, "商家", "FGS"),
    STORE(3, 21,6, "店铺", "DP"),
    ;

    /**
     * 组织架构定义的组织级别类型
     */
    private final Integer odyType;
    /**
     * 券活动coupon_theme表对应的组织级别类型
     */
    private final Integer themeType;
    /**
     * mkt_use_rule表对应的组织级别类型
     */
    private final Integer ruleType;

    private final String orgLevelName;

    private final String orgLevelCode;

    OrgLevelEnum(Integer odyType, Integer themeType, Integer ruleType, String orgLevelName, String orgLevelCode) {
        this.odyType = odyType;
        this.themeType = themeType;
        this.ruleType = ruleType;
        this.orgLevelName = orgLevelName;
        this.orgLevelCode = orgLevelCode;
    }

    public Integer getOdyType() {
        return odyType;
    }

    public Integer getThemeType() {
        return themeType;
    }

    public Integer getRuleType() {
        return ruleType;
    }

    public String getOrgLevelName() {
        return orgLevelName;
    }

    public String getOrgLevelCode() {
        return orgLevelCode;
    }

    public static OrgLevelEnum getEnumByThemeType(Integer themeType) {

        return Stream.of(values())
                .filter(bean -> bean.themeType.equals(themeType))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("OrgLevelEnum: [themeType=" + themeType + "]不存在！"))
                ;
    }
}
