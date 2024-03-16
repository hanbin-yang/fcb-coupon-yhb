package com.fcb.coupon.backend.model.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CouponUserConditionDto {

    private Long themeId;

    private List<String> userIdList;

    private List<Integer> statusList;

    private Date startTime;

    private Date endTime;
}
