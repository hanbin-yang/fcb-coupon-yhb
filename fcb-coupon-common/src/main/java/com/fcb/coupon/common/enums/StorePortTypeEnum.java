package com.fcb.coupon.common.enums;

import java.util.Objects;

public enum StorePortTypeEnum {
    BUILD_ONLINE_STATUS(0, "BUILD_ONLINE_STATUS"),
    ORG_POINT_BUILD_ONLINE_STATUS(1, "ORG_POINT_BUILD_ONLINE_STATUS"),
    CPOINT_BUILD_ONLINE_STATUS(2, "CPOINT_BUILD_ONLINE_STATUS"),
    ;
    private Integer userType;
    private String storePortType;

    public Integer getUserType() {
        return userType;
    }

    public String getStorePortType() {
        return storePortType;
    }

    StorePortTypeEnum(Integer userType, String storePortType) {
        this.userType = userType;
        this.storePortType = storePortType;
    }

    public static String getStorePortTypeByUserType(Integer userType) {
        if (userType != null) {
            for (StorePortTypeEnum storePortTypeEnum : StorePortTypeEnum.values()) {
                if (Objects.equals(storePortTypeEnum.getUserType(), userType)) {
                    return storePortTypeEnum.getStorePortType();
                }
            }
        }
        return null;
    }

}
