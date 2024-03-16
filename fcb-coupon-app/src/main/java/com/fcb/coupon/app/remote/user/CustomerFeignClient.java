package com.fcb.coupon.app.remote.user;


import com.fcb.coupon.app.remote.dto.input.CustomerByUnionIdInput;
import com.fcb.coupon.app.remote.dto.input.CustomerHdTokenInfoInput;
import com.fcb.coupon.app.remote.dto.input.CustomerIdInput;
import com.fcb.coupon.app.remote.dto.input.CustomerInfoSimpleInput;
import com.fcb.coupon.app.remote.dto.output.CustomerByUnionIdOutput;
import com.fcb.coupon.app.remote.dto.output.CustomerIdInfoOutput;
import com.fcb.coupon.app.remote.dto.output.CustomerUserInfo;
import com.fcb.coupon.app.remote.dto.output.CustomerInfoSimpleOutput;
import com.fcb.coupon.common.constant.RestConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import feign.HeaderMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

/**
 * C端 用户信息接口
 */
@FeignClient(value = "customer", url = "${remote.url.customer.domain}")
public interface CustomerFeignClient {

    /**
     * 根据电话号码批量获取C端用户信息
     */
    @PostMapping(value = RestConstant.GET_REGISTER_USER_INFO_LIST_URL)
    ResponseDto<List<CustomerInfoSimpleOutput>> listCustomerInfoByPhones(@RequestBody CustomerInfoSimpleInput param);

    /**
     * 根据unionId获取C端用户信息
     *
     * @param in in
     * @return C端用户信息
     */
    @PostMapping(value = RestConstant.GET_CUSTOMER_INFO_BY_UNIONID_URL)
    ResponseDto<CustomerByUnionIdOutput> getCustomerInfoByUnionId(@RequestBody CustomerByUnionIdInput in);

    /**
     * 根据hdToken和terminalType获取C端用户信息 (用于微吼直播)
     */
    @PostMapping(value = RestConstant.C_LOGIN_BY_UT, consumes = "application/json;charset=UTF-8")
    ResponseDto<CustomerUserInfo> getCustomerInfoByHdTokenAndTerminalType(@RequestBody CustomerHdTokenInfoInput in);

    @PostMapping(value = RestConstant.C_TOKEN_VALIDATE, consumes = "application/json;charset=UTF-8")
    ResponseDto<String> innerCheckAccessToken(@RequestBody Map<String, Object> in, @RequestHeader Map<String, String> headers);

    /**
     * 根据customerId查询C客户信息
     */
    @PostMapping(value = RestConstant.GET_CUSTOMER_INFO_BY_CUSTOMERID_URL)
    ResponseDto<CustomerIdInfoOutput> getCustomerInfoByCustomerId(@RequestBody CustomerIdInput param);
}
