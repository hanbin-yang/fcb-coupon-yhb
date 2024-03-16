package com.fcb.coupon.common.enums;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author HanBin_Yang
 * @since 2021/6/25 8:56
 */
public enum AsyncTaskStatusEnum {
    WAITING(0, "待执行"),
    SUCCESS(1, "执行成功"),
    FAIL(2, "执行失败"),
    ;
    private final Integer status;

    private final String desc;

    AsyncTaskStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static AsyncTaskStatusEnum of(Integer status) {
        Objects.requireNonNull(status);

        return Stream.of(values())
                .filter(bean -> bean.status.equals(status))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("error AsyncTaskStatusEnum: status [" + status + "] 不存在"));
    }
}
