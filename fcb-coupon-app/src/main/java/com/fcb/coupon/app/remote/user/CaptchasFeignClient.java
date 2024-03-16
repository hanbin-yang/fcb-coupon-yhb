package com.fcb.coupon.app.remote.user;

import com.fcb.coupon.common.constant.RestConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author mashiqiong
 * @date 2021-8-19 18:40
 */
@FeignClient(value = "captchas", url = "${remote.url.middleend.adminportal}")
public interface CaptchasFeignClient {

    @PostMapping(value = RestConstant.SMS_VOICE_SEND)
    ResponseDto<Boolean> voiceSend(@RequestBody Map<String, Object> param);
}
