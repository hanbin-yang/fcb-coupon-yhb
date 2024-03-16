package com.fcb.coupon.backend.remote.client;

import com.fcb.coupon.backend.remote.dto.input.AgencyInfoInputDto;
import com.fcb.coupon.backend.remote.dto.input.BrokerIdInput;
import com.fcb.coupon.backend.remote.dto.input.BrokerInfoSimpleInputDto;
import com.fcb.coupon.backend.remote.dto.input.UnionIdRequest;
import com.fcb.coupon.backend.remote.dto.out.AgencyInfoOutputDto;
import com.fcb.coupon.backend.remote.dto.out.BrokerInfoByUnionIdResponse;
import com.fcb.coupon.backend.remote.dto.out.BrokerInfoDto;
import com.fcb.coupon.backend.remote.dto.out.BrokerInfoSimpleDto;
import com.fcb.coupon.common.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Broker中心用户（会员、机构账户）接口
 *
 * @Author WeiHaiQi
 * @Date 2021-06-21 21:27
 **/
@FeignClient(value = "OrgBroker", url = "${remote.url.broker.domain}")
public interface BrokerClient {

    /**
     * 根据电话号码批量获取会员用户信息（B端）
     */
    @PostMapping(value = "/omc/api/broker/data/v1/getRegisterUserInfoList", consumes = "application/json;charset=UTF-8")
    ResponseDto<List<BrokerInfoSimpleDto>> getBrokerInfoListByPhones(@RequestBody BrokerInfoSimpleInputDto param);

    /**
     * 通过orgAccount获取机构经纪人信息（机构端）
     */
    @PostMapping(value = "/omc/api/broker/data/v1/getBrokerListByAccounts", consumes = "application/json;charset=UTF-8")
    ResponseDto<List<AgencyInfoOutputDto>> getBrokerListByAccounts(@RequestBody AgencyInfoInputDto inputDto);

    /**
     * 根据brokerId批量获取会员用户信息（B端）
     */
    @PostMapping(value = "/omc/api/broker/data/v1/brokerInfo/list", consumes = "application/json;charset=UTF-8")
    ResponseDto<List<BrokerInfoDto>> getBrokerInfoListByBrokerIds(@RequestBody BrokerIdInput param);


    @PostMapping(value = "/omc/api/broker/data/v1/findBrokerInfoByUnionId", consumes = "application/json;charset=UTF-8")
    ResponseDto<BrokerInfoByUnionIdResponse> findBrokerInfoByUnionId(@RequestBody UnionIdRequest param);


}
