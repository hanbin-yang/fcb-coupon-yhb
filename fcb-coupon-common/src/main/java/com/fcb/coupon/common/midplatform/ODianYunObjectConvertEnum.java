package com.fcb.coupon.common.midplatform;

import java.util.Objects;

/**
 * 类描述 : 欧电云对象转换成自己的对象
 *
 * @author youxin.wu
 * @version 1.0
 * @date 2021-7-30 14:00
 */
public enum ODianYunObjectConvertEnum {
    AUTH_MERCHANT_INFO("com.odianyun.user.client.model.dto.AuthMerchantDTO", "com.fcb.coupon.common.dto.AuthMerchantDTO"),

    AUTH_STORE_INFO("com.odianyun.user.client.model.dto.AuthStoreDTO", "com.fcb.coupon.common.dto.AuthStoreDTO"),

    FUNCTION_PATH_TREE_INFO("com.odianyun.user.client.model.dto.FunctionPathTreeDTO", "com.fcb.coupon.common.dto.FunctionPathTreeDTO"),

    MERCHANT_INFO("com.odianyun.user.client.model.dto.MerchantInfo", "com.fcb.coupon.common.dto.MerchantInfo"),

    CHANNEL_INFO("com.odianyun.user.client.model.dto.ChannelInfoOutDTO", "com.fcb.coupon.common.dto.ChannelInfoOutDTO"),

    USER_INFO("com.odianyun.user.client.model.dto.UserInfo", "com.fcb.coupon.common.dto.UserInfo"),

    FUNCTION_INFO("com.odianyun.user.client.model.dto.FunctionInfo", "com.fcb.coupon.common.dto.FunctionInfo"),

    ROLE_INFO("com.odianyun.user.client.model.dto.RoleDTO", "com.fcb.coupon.common.dto.RoleDTO"),

    STORE_INFO_INFO("com.odianyun.user.client.model.dto.StoreInfo", "com.fcb.coupon.common.dto.StoreInfo"),
    ;


    ODianYunObjectConvertEnum(String oDianYunObject, String fpcObject) {
        this.oDianYunObject = oDianYunObject;
        this.realObject = fpcObject;
    }

    private final String oDianYunObject;

    private final String realObject;

    public String getODianYunObject() {
        return oDianYunObject;
    }

    public String getRealObject() {
        return realObject;
    }

    public static String getFpcObject(String oDianYunObject){
        for (ODianYunObjectConvertEnum typeEnum : ODianYunObjectConvertEnum.values()) {
            if (Objects.equals(typeEnum.getODianYunObject(), oDianYunObject)) {
                return typeEnum.getRealObject();
            }
        }
        return oDianYunObject;
    }

}