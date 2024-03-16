package com.fcb.coupon.common.util;

public interface RStock {
    /**
     * 扣减库存策列
     * 库存>0但不够扣时的策列
     */
    enum DeductMode {
        NO(0, "不扣减"),
        DECR(1, "扣减剩余库存"),
        ;
        private final int flag;
        private final String desc;

        DeductMode(int flag, String desc) {
            this.flag = flag;
            this.desc = desc;
        }

        public int getFlag() {
            return flag;
        }

        public String getDesc() {
            return desc;
        }
    }
}
