package com.fcb.coupon.app.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * TODO
 *
 * @Author WeiHaiQi
 * @Date 2021-08-23 19:06
 **/
@Data
public class CouponThemeListHouseBo implements Serializable {

    /**
     * 券活动ID
     */
    private List<Long> ids;
    /**
     * 用户UnionID
     */
    private String unionId;
    /**
     * 用户类型，
     * B_USER--B端用户;
     * C_USER--C端用户;
     * SAAS_USER--SAAS端用户;
     * J_USER--机构用户;
     */
    private String clientType;
}
