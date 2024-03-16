package com.fcb.coupon.common.enums;

import java.util.Objects;
import java.util.stream.Stream;

public enum AsyncStatusEnum {

    SENDING(0, "发送中"),
    FINISHED(1, "发送完成"),
    FAIL(2, "发送失败"),
    ;

    private final Integer status;

    private final String desc;

    AsyncStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static AsyncStatusEnum of(Integer status) {
        Objects.requireNonNull(status);

        return Stream.of(values())
                .filter(bean -> bean.status.equals(status))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("error AsyncStatusEnum: status [" + status + "] 不存在"));
    }
}
