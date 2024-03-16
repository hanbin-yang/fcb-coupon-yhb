package com.fcb.coupon.app.model.bo;

import com.fcb.coupon.app.model.PageDto;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author 唐陆军
 * @Description 用户优惠券查询参数
 * @createTime 2021年08月27日 11:39:00
 */
@Data
public class CouponUserListBo {

    private String userId;

    private Integer userType;

    private List<Integer> statusList;

    private Integer couponDiscountType;

    /*
     * @description 排序方式 0:绑定时间降序，1：过期时间升序
     * @author 唐陆军
     * @date 2021-8-27 14:24
     */
    private Integer sortedBy;

    private Date endTime;
}
