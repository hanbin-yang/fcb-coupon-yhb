package com.fcb.coupon.app.remote.user;

import com.fcb.coupon.app.remote.dto.input.SaasUserInfoInput;
import com.fcb.coupon.app.remote.dto.output.SaasUserInfoOutput;
import com.fcb.coupon.common.constant.RestConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.dto.SaasUserLoginChectDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author mashiqiong
 * @date 2021-8-20 21:43
 */
@FeignClient(value = "saas", url = "${remote.url.saas.api.domain}")
public interface SaasFeignClient {

    @PostMapping(value = RestConstant.SASS_UER_LOGIN_CHECK)
    ResponseDto<SaasUserLoginChectDto> checkTokenValid4saas(String token);

    @PostMapping(value = "/saasapigateway/api/thirdplat/params?apiNumber=001_checkTokenValid")
    ResponseDto<SaasUserLoginChectDto> checkTokenValid4saas(@RequestBody SaasUserInfoInput input);

    @PostMapping(value = "/saasapigateway/api/thirdplat/params?apiNumber=001_validTokenUser")
    SaasUserInfoOutput getAgentInfoById(@RequestBody SaasUserInfoInput input);
    
}
