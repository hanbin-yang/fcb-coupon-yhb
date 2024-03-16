package com.fcb.coupon.app.remote.dto.output;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月24日 17:08:00
 */
@Data
public class CustomerIdInfoOutput implements Serializable {

    private Long customerId;

    private String phone;

    private String photo;

    private String nickName;

    private String approveStatus;
}
