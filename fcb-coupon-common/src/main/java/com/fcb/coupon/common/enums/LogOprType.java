package com.fcb.coupon.common.enums;

import java.util.stream.Stream;

/**
 * @author YangHanBin
 * @date 2021-06-16 18:29
 */
public enum LogOprType {
    CREATE(1,"新建"),
    SUBMIT_AUDIT(2,"提交审核"),
    AUDIT(3,"审核"),
    REJECT(4,"驳回"),
    GENERATE_COUPONS(5,"生券"),
    EDIT(6,"编辑"),
    COPY(7,"复制"),
    CLOSE(8,"关闭"),
    ISSUING_COUPONS(9,"发券"),
    IMPORT_COUPONS(10,"导入券码"),
    DELETE(11,"删除"),
    VIEW(12,"查看"),
    UPDATE_THEME_AFTER_CHECK(14, "更新规则"),
    INVALID(101,"作废"),
    FREEZE(102,"冻结"),
    UNFREEZE(103,"解冻"),
    EXTENSION(104,"延期"),
    VERIFICATION(105,"优惠券核销"),
    COUPON_LOCK_NO(106, "优惠券上锁"),
    COUPON_UNLOCK_NO(107, "优惠券解锁"),
    COUPON_REBIND_NO(108, "优惠券换绑"),
    MINGY_YUAN_INVALID(109, "明源作废/挞定"), // 明源作废/挞定
    ;

    private final Integer type;
    private final String desc;

    LogOprType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static LogOprType of(Integer type) {

        return Stream.of(values())
                .filter(bean -> bean.type.equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("LogOprType: [type=" + type + "]不存在！"))
                ;
    }
}
