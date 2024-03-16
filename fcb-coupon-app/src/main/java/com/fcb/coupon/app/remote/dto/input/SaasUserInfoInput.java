package com.fcb.coupon.app.remote.dto.input;

import lombok.Data;

import java.io.Serializable;

@Data
public class SaasUserInfoInput implements Serializable {

    private String token;
}
