package com.fcb.coupon.backend.model.bo;


import lombok.Data;

/*
优惠券发券userid参数
 */
@Data
public class CouponSendUserBo {

    /*
    用户id
     */
    private String userId;

        /*
    unionId
     */
    private String unionId;

        /*
    手机号
     */
    private String phone;

    /*
    数量
     */
    private Integer count;


    private String transactionId;
}
