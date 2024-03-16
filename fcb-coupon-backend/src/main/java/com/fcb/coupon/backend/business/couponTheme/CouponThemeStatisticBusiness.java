package com.fcb.coupon.backend.business.couponTheme;

import com.fcb.coupon.backend.model.param.response.CouponThemeStatisticsResponse;

import java.util.List;

public interface CouponThemeStatisticBusiness {

    List<CouponThemeStatisticsResponse> listByThemeIds(List<Long> themeIds);

}
