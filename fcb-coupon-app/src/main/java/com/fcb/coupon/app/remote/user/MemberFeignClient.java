package com.fcb.coupon.app.remote.user;

import com.fcb.coupon.app.remote.dto.BrokerInfoDto;
import com.fcb.coupon.app.remote.dto.input.BrokerInfoByUnionIdInput;
import com.fcb.coupon.app.remote.dto.input.BrokerInfoSimpleInputDto;
import com.fcb.coupon.app.remote.dto.input.MemberHdTokenInfoInput;
import com.fcb.coupon.app.remote.dto.output.BrokerInfoSimpleDto;
import com.fcb.coupon.app.remote.dto.output.MemberUserInfo;
import com.fcb.coupon.common.constant.RestConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import feign.HeaderMap;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

/**
 * B端（会员）用户信息接口
 *
 * @Author WeiHaiQi
 * @Date 2021-08-13 14:32
 **/
@FeignClient(value = "OrgBroker",url = "${remote.url.broker.domain}")
public interface MemberFeignClient {
    /**
     * 根据电话号码批量获取会员用户信息（B端）
     */
    @PostMapping(value = RestConstant.GET_BROKER_INFO_BY_PHONE_URL,consumes = "application/json;charset=UTF-8")
    ResponseDto<List<BrokerInfoSimpleDto>> getBrokerInfoListByPhones(@RequestBody BrokerInfoSimpleInputDto param);

    /**
     * 根据unionId查询用户信息
     */
    @PostMapping(value = "/omc/api/broker/data/v1/findBrokerInfoByUnionId", consumes = "application/json;charset=UTF-8")
    ResponseDto<BrokerInfoDto> findBrokerInfoByUnionId(@RequestBody BrokerInfoByUnionIdInput param);

    /**
     * 根据hdToken和terminalType获取B端用户信息 (用于微吼直播)
     */
    @PostMapping(value = RestConstant.B_LOGIN_BY_UT, consumes = "application/json;charset=UTF-8")
    ResponseDto<MemberUserInfo> getMemberInfoByHdTokenAndTerminalType(@RequestBody MemberHdTokenInfoInput in);

    @PostMapping(value = RestConstant.B_TOKEN_VALIDATE, consumes = "application/json;charset=UTF-8")
    ResponseDto<Void> innerCheckAccessToken(@RequestBody Map<String, Object> in, @RequestHeader Map<String, String> headers);
}
