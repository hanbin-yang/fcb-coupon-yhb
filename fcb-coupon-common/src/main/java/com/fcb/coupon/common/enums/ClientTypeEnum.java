package com.fcb.coupon.common.enums;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 客户端类型
 *
 * @Author WeiHaiQi
 * @Date 2021-08-16 17:30
 **/
public enum ClientTypeEnum {
    /**
     * 客户端类型
     */
    B("B_USER", "B端用户"),
    C("C_USER", "C端用户"),
    SAAS("SAAS_USER", "SAAS用户"),
    ;

    ClientTypeEnum(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    private final String key;
    private final String desc;

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }


    public static ClientTypeEnum of(String key) {
        Objects.requireNonNull(key);

        return Stream.of(values())
                .filter(bean -> bean.key.equals(key))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("error ClientTypeEnum: key [" + key + "] 不存在"));
    }

    public static Boolean  contains(String values){
        if(values == null){
            return false;
        }
        for(ClientTypeEnum typeEnum:ClientTypeEnum.values()){
            if(typeEnum.key.equals(values)){
                return true;
            }
        }
        return false;
    }
}
