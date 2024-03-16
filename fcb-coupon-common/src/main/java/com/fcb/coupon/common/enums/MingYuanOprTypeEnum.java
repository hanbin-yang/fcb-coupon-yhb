package com.fcb.coupon.common.enums;

import java.util.Objects;

/**
 * @author YangHanBin
 * @date 2021-08-24 10:16
 */
public enum MingYuanOprTypeEnum {
    //1认购态换房 2认购态作废 3认购态退房/挞定 4签约态作废 5签约态退房/挞定 6重置认购态换房 7重置认购态作废 8重置认购态退房/挞定 9认购转签约 10重置认购态转签约态 11保存为认购态
    REBIND_FOR_SUB(1, "认购态换房"),
    INVALID_FOR_SUB(2, "认购态作废"),
    TART_FOR_SUB(3, "认购态退房/挞定"),
    INVALID_FOR_SIGN(4, "签约态作废"),
    TART_FOR_SIGN(5, "签约态退房/挞定"),
    REBIND_FOR_RESET_SUB(6, "重置认购态换房"),
    INVALID_FOR_RESET_SUB(7, "重置认购态作废"),
    TART_FOR_RESET_SUB(8, "重置认购态退房/挞定"),
    SUB_TO_SIGN(9, "认购转签约"),
    RETURN_TO_SUB(10, "重置认购态转签约态"),
    SAVE_TO_SUB(11, "保存为认购态"),
    ;
    private Integer type;
    private String desc;

    public static String getDescByType(int type) {
        for (MingYuanOprTypeEnum value : MingYuanOprTypeEnum.values()) {
            if (Objects.equals(value.getType(), type)) {
                return value.getDesc();
            }
        }
        return null;
    }

    MingYuanOprTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}

