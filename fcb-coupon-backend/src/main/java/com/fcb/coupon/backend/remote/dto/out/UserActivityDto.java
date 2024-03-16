package com.fcb.coupon.backend.remote.dto.out;

import lombok.Data;

import java.util.List;

/**
 *
 * @Author WeiHaiQi
 * @Date 2021-06-21 23:06
 **/
@Data
public class UserActivityDto {

    private String unionId;

    private List<ActivityOutDto> activityList;
}
