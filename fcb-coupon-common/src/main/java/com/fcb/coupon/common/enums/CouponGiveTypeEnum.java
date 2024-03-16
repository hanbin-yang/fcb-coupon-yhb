package com.fcb.coupon.common.enums;

public enum CouponGiveTypeEnum {
    /**
     * 赠送类型
     */
	 TYPE_SMS(1, "短信赠送"),
	TYPE_FACE(2, "面对面赠送"),
	TYPE_FRIENDS(3, "微信朋友圈分享"),
    ;
    private Integer type;
    private String text;

    CouponGiveTypeEnum(Integer type, String text) {
        this.type = type;
        this.text = text;
    }

    public Integer getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    /**
     * 枚举是否包含当前值
     * @param values
     * @return
     */
    public static Boolean  contains(Integer values){
        if(values == null){
            return false;
        }
        for(CouponGiveTypeEnum typeEnum:CouponGiveTypeEnum.values()){
            if(typeEnum.type.equals(values)){
                return true;
            }
        }
        return false;
    }

}
