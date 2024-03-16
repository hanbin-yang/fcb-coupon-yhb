package com.fcb.coupon.backend.remote.dto.out;

import lombok.Data;

import java.io.Serializable;


@Data
public class BrokerInfoByUnionIdResponse implements Serializable {


    private String brokerId;

    private String mphone;

    private String name;

    private String unionId;

}
