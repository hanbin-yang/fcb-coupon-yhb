package com.fcb.coupon.app.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 领券备忘录
 * @author YangHanBin
 * @date 2021-08-21 9:51
 */
@Data
@Accessors(chain = true)
public class UserReceiveMemento {
    private Long couponThemeId;

    private String userId;

    private Integer userType;

    /**
     * 当前真实领券数量
     */
    private Integer realReceiveCount;
    /**
     * 旧的当月领券数
     */
    private Integer oldMonthCount;
    /**
     * 旧的当天领券数
     */
    private Integer oldDayCount;
    /**
     * 当前领券时间, 时间戳
     */
    private Long currReceiveDate;
    /**
     * 旧的最后领券时间, 时间戳
     */
    private Long oldLastReceiveDate;
}
