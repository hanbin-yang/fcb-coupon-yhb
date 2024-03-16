package com.fcb.coupon.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.app.model.dto.RealUserInfoDto;
import com.fcb.coupon.app.remote.dto.BrokerInfoDto;
import com.fcb.coupon.app.remote.dto.input.BrokerInfoByUnionIdInput;
import com.fcb.coupon.app.remote.dto.input.CustomerByUnionIdInput;
import com.fcb.coupon.app.remote.dto.output.CustomerByUnionIdOutput;
import com.fcb.coupon.app.remote.user.CustomerFeignClient;
import com.fcb.coupon.app.remote.user.MemberFeignClient;
import com.fcb.coupon.app.service.UserFacadeService;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.ClientTypeEnum;
import com.fcb.coupon.common.enums.UserTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 用户中心用户查询接口代理服务实现
 *
 * @Author WeiHaiQi
 * @Date 2021-08-16 17:18
 **/
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class UserFacadeServiceImpl implements UserFacadeService {

    private final CustomerFeignClient customerClient;
    private final MemberFeignClient orgBrokerInfoClient;

    @Override
    public RealUserInfoDto getRealUserInfo(String clientType, String unionId) {
        RealUserInfoDto realUserInfo = new RealUserInfoDto();
        if (StringUtils.equals(clientType, ClientTypeEnum.C.getKey())) {
            CustomerByUnionIdOutput customerInfo = getCustomerInfoByUnionId(unionId);
            if (Objects.isNull(customerInfo) || StringUtils.isBlank(customerInfo.getCustomerId())) {
                log.info("未查询到unionId对应的C端用户 unionId={}",unionId);
                return null;
            }

            // C端用户coupon_user表里面存的是customerId;
            realUserInfo.setMobilePhone(customerInfo.getMphone());
            realUserInfo.setRealUserId(customerInfo.getCustomerId().trim());
            realUserInfo.setName(customerInfo.getName());
            realUserInfo.setUserType(UserTypeEnum.C.getUserType());
        } else {
            /**
             * 非C端的默认为B端
             */
            BrokerInfoDto currentBroker = getBrokerInfoByUnionId(unionId);
            if (Objects.isNull(currentBroker)) {
                log.info("未查询到unionId对应的B端用户 unionId={}",unionId);
            }

            realUserInfo.setName(currentBroker.getName());
            realUserInfo.setMobilePhone(currentBroker.getMphone());
            if (StringUtils.isNotBlank(currentBroker.getBrokerId())) {
                realUserInfo.setRealUserId(currentBroker.getBrokerId());
            }

            if (StringUtils.equals(currentBroker.getBrokerType(), UserTypeEnum.SAAS.getUserType() + "")) {
                realUserInfo.setUserType(UserTypeEnum.SAAS.getUserType());
                realUserInfo.setMobilePhone(currentBroker.getOrgAccount());
            } else {
                realUserInfo.setUserType(UserTypeEnum.B.getUserType());
            }
        }

        return realUserInfo;
    }

    @Override
    public RealUserInfoDto getCacheRealUserInfo(String clientType, String unionId) {
        return getRealUserInfo(clientType,unionId);
    }

    /**
     *根据unionId获取C端用户的信息
     *
     * @param unionId
     * @return
     */
    private CustomerByUnionIdOutput getCustomerInfoByUnionId(String unionId) {
        try {
            log.info("调用经纪人中心，根据unionId查询C端用户信息，入参：unionId={}", unionId);
            CustomerByUnionIdInput input = new CustomerByUnionIdInput();
            input.setUnionId(unionId);
            ResponseDto<CustomerByUnionIdOutput> customerResponse = customerClient.getCustomerInfoByUnionId(input);
            log.info("调用经纪人中心，根据unionId查询C端用户信息，unionId={}, 出参：{}", unionId, JSON.toJSONString(customerResponse));
            return customerResponse.getData();
        } catch (Exception e) {
            log.error("根据unionId查询用户信息失败：unionId={} {}", unionId, e);
        }
        return null;
    }

    /**
     * 根据unionId查询Broker用户信息
     * @param unionId
     * @return
     */
    private BrokerInfoDto getBrokerInfoByUnionId(String unionId) {
        try {
            log.info("调用经纪人中心，根据unionId:{}查询Broker用户信息",unionId);
            BrokerInfoByUnionIdInput input = new BrokerInfoByUnionIdInput();
            input.setUnionId(unionId);
            ResponseDto<BrokerInfoDto> result = orgBrokerInfoClient.findBrokerInfoByUnionId(input);
            log.info("调用经纪人中心，根据unionId:{}查询Broker用户信息，出参：{}",unionId, JSON.toJSONString(result));

            BrokerInfoDto brokerInfoDto = result.getData();
            return brokerInfoDto;
        } catch (Exception e) {
            log.error("根据unionId查询用户信息失败：{} {}",unionId,e);
        }
        return null;
    }
}
