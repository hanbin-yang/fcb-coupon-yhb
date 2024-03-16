package com.fcb.coupon.common.enums;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 用户类型
 *
 * @Author Weihaiqi
 * @Date 2021-06-16 20:37
 **/
public enum UserTypeEnum {
    B(0, "会员"),
    SAAS(1, "SAAS用户"),
    C(2, "C端用户"),
    ;

    private final Integer userType;
    private final String userTypeStr;

    UserTypeEnum(Integer userType, String userTypeStr) {
        this.userType = userType;
        this.userTypeStr = userTypeStr;
    }

    public Integer getUserType() {
        return userType;
    }

    public String getUserTypeStr() {
        return userTypeStr;
    }

    public static String getStrByUserType(Integer userType) {
        if (userType != null) {
            for (UserTypeEnum couponThemeEnum : UserTypeEnum.values()) {
                if (Objects.equals(couponThemeEnum.getUserType(), userType)) {
                    return couponThemeEnum.getUserTypeStr();
                }
            }
        }
        return null;
    }

    public static UserTypeEnum of(Integer userType) {
        return Stream.of(values())
                .filter(bean -> bean.userType.equals(userType))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("用户类型: [userType=" + userType + "]不存在！"));
    }
}
