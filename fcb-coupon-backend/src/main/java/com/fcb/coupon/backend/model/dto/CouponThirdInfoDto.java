package com.fcb.coupon.backend.model.dto;

import lombok.Data;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月18日 16:24:00
 */
@Data
public class CouponThirdInfoDto {

    /*
     * @description 第三方券码/卡号
     */
    private String thirdCouponCode;

    /*
     * @description 第三方券码的密码
     */
    private String thirdCouponPassword;

}
