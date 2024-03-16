package com.fcb.coupon.backend.remote.dto.out;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月24日 18:58:00
 */
@Data
public class BrokerInfoDto implements Serializable {

    private String brokerId;

    private String brokerName;

    private String phone;

}
