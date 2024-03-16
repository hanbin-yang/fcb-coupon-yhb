package com.fcb.coupon.app.remote.dto.output;


import lombok.Data;

import java.io.Serializable;

@Data
public class SaasUserInfoOutput implements Serializable {

    // 这两个字段被网关过滤了，获取不到
    /*private Integer resultType;

    private String resultMsg;*/

    private SaasUserInfo data;

    @Data
    public static class SaasUserInfo implements Serializable {

        private Integer state;
        private String name;
        private String phone;
        private String personId;
    }

}
