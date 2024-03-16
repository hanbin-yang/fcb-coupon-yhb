package com.fcb.coupon.backend.model.param.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 唐陆军
 * @Description 发券消息参数
 * @createTime 2021年08月16日 19:21:00
 */
@Data
public class CouponSendMessageRequest implements Serializable {

    private CouponSendDetailMessageRequest data;
}
