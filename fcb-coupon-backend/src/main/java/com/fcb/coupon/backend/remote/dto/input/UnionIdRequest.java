package com.fcb.coupon.backend.remote.dto.input;

import lombok.Data;

import java.io.Serializable;

@Data
public class UnionIdRequest implements Serializable {

    private String unionId;
}
