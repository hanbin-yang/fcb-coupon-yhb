package com.fcb.coupon.backend.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CouponThemeCrowdScopeIdDto implements Serializable {

    private List<Integer> ids;
}
