package com.fcb.coupon.client.backend.feign;

import com.fcb.coupon.client.backend.param.request.ActivitySendCouponRequest;
import com.fcb.coupon.client.backend.param.response.ActivitySendCouponResponse;
import com.fcb.coupon.client.dto.RestResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * @author 唐陆军
 * @Description 优惠券后台对外接口
 * @createTime 2021年06月09日 18:46:00
 */
@FeignClient(value = "fcb-coupon-backend")
public interface CouponBackendClient {

    @ApiOperation(value = "活动发券")
    @PostMapping(value = "/cloud/couponBackWrite/sendCoupons4activity")
    RestResult<ActivitySendCouponResponse> sendActivityCoupon(@Valid @RequestBody ActivitySendCouponRequest request);


    @ApiOperation(value = "通过活动ID列表查询活动详情")
    @PostMapping(value = "/cloud/couponThemeBackReadService/getCouponThemeDetailsByThemeIds")
    RestResult<ActivitySendCouponResponse> getCouponThemeDetailsByThemeIds(@Valid @RequestBody ActivitySendCouponRequest request);

}
