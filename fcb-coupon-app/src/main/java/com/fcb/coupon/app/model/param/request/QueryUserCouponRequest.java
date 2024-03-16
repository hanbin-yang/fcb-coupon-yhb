package com.fcb.coupon.app.model.param.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 查用户券请求
 *
 * @Author WeiHaiQi
 * @Date 2021-08-13 10:18
 **/
@Data
public class QueryUserCouponRequest implements Serializable {

    private static final long serialVersionUID = -7980979301848959107L;
    private List<String> unionIdList;
}
