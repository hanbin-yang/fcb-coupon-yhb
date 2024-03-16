package com.fcb.coupon.app.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CouponThemeCrowdScopeIdDto implements Serializable {

    private List<Integer> ids;

}
