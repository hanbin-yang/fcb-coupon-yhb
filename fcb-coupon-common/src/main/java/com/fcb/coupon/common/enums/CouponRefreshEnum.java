package com.fcb.coupon.common.enums;

import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 *  @Author WeiHaiQi
 * @Date 2021-07-14 17:25
 **/
public enum  CouponRefreshEnum {

    BY_COUPON_THEME_ID("refreshByCouponThemeId", "通过券活动id全量刷"),
    BY_USER_PHONE_AND_USER_TYPE("refreshByUserPhoneAndUserType", "通过手机号和用户类型刷"),
    BY_COUPON_ID("refreshByCouponId", "通过券id刷"),
    BY_COUPON_ALL("refreshAllCoupon", "刷新全部券"),
    ;

    @Getter
    private String flag;
    @Getter
    private String desc;

    CouponRefreshEnum(String flag, String desc) {
        this.flag = flag;
        this.desc = desc;
    }

    public static CouponRefreshEnum getEnumByFlag(String flag) throws BusinessException {
        for (CouponRefreshEnum item : CouponRefreshEnum.values()) {
            if (StringUtils.equals(item.getFlag(), flag)) {
                return item;
            }
        }

        throw new BusinessException(CommonErrorCode.SYSTEM_ERROR.getCode(),"refreshType不符合要求");
    }
}
