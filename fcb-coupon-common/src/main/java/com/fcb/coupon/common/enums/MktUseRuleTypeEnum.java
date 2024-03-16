package com.fcb.coupon.common.enums;

import java.util.stream.Stream;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 15:17
 */
public enum MktUseRuleTypeEnum {
    GROUP(11, "集团", "ZB"),
    MERCHANT(1, "商家", "FGS"),
    STORE(6, "店铺", "DP"),
    ;

    private final Integer type;

    private final String orgLevelName;

    private final String orgLevelCode;

    MktUseRuleTypeEnum(Integer type, String orgName, String orgLevelCode) {
        this.type = type;
        this.orgLevelName = orgName;
        this.orgLevelCode = orgLevelCode;
    }

    public Integer getType() {
        return type;
    }

    public String getOrgLevelName() {
        return orgLevelName;
    }

    public String getOrgLevelCode() {
        return orgLevelCode;
    }

    public static MktUseRuleTypeEnum of(Integer type) {

        return Stream.of(values())
                .filter(bean -> bean.type.equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("MktUseRuleTypeEnum: [type=" + type + "]不存在！"))
                ;
    }

    public static String getOrgLevelNameByType(Integer type) {

        return Stream.of(values())
                .filter(bean -> bean.type.equals(type))
                .findAny()
                .map(MktUseRuleTypeEnum::getOrgLevelName)
                .orElseThrow(() -> new IllegalArgumentException("MktUseRuleTypeEnum: [type=" + type + "]不存在！"))
                ;
    }

    public static Integer getTypeByDesc(String desc) {

        return Stream.of(values())
                .filter(bean -> bean.orgLevelName.equals(desc))
                .findAny()
                .map(MktUseRuleTypeEnum::getType)
                .orElseThrow(() -> new IllegalArgumentException("MktUseRuleTypeEnum: [desc=" + desc + "]不存在！"))
                ;
    }
}
