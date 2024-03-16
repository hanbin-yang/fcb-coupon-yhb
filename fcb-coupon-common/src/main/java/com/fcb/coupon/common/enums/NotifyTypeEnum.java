package com.fcb.coupon.common.enums;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月25日 15:31:00
 */
public enum NotifyTypeEnum {

    SMS("SMS", "短信"),
    PUSH("PUSH", "站内消息"),
    IMAIL("IMAIL", "邮件"),
    ;

    NotifyTypeEnum(String key, String desc) {
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

}
