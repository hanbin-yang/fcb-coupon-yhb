package com.fcb.coupon.app.remote.user;

import com.fcb.coupon.app.model.dto.ProfileDto;
import com.fcb.coupon.common.constant.RestConstant;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @author mashiqiong
 * @date 2021-8-20 21:12
 */
@FeignClient(value = "appapi", url = "${remote.url.m.broker}")
public interface ProfileFeignClient {
    @PostMapping(value = RestConstant.GET_BROKER_PROFILE)
    @Headers(value = {"Content-Type: application/json", "authorization:{authorization},unionid:{unionid},terminalType:{terminalType}"})
    ProfileDto getBrokerProfile(@RequestHeader("authorization") String authorization, @RequestHeader("unionid") String unionId, @RequestHeader("terminalType") String terminalType);
}
