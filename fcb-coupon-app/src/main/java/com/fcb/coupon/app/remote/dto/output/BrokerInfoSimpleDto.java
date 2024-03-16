package com.fcb.coupon.app.remote.dto.output;

import lombok.Data;

/**
 * 经纪人中心用户信息（简化）
 *
 * @Author WeiHaiQi
 * @Date 2021-06-21 21:50
 **/
@Data
public class BrokerInfoSimpleDto {

    /**
     * 经纪人用户id
     */
    private String brokerId;
    /**
     * 统一用户ID
     */
    private String unionId;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 手机号
     */
    private String phoneNo;
    /**
     * 会员类型
     */
    private Long brokerType;
    /**
     * 是否禁用0:否 1:是
     */
    private Integer isDisabled;
}
