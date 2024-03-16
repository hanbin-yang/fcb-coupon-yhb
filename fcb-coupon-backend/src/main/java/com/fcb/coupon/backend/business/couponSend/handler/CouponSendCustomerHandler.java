package com.fcb.coupon.backend.business.couponSend.handler;

import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.remote.client.CustomerClient;
import com.fcb.coupon.backend.remote.dto.input.CustomerIdInput;
import com.fcb.coupon.backend.remote.dto.input.CustomerInfoSimpleInput;
import com.fcb.coupon.backend.remote.dto.out.CustomerIdInfoOutput;
import com.fcb.coupon.backend.remote.dto.out.CustomerInfoSimpleOutput;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 121272100
 * 处理发送给C端用户优惠券
 */
@Slf4j
@Component
public class CouponSendCustomerHandler extends AbstractCouponSendHandler {

    @Autowired
    private CustomerClient customerClient;

    @Override
    public Boolean supports(Integer sendUserType) {
        return UserTypeEnum.C.getUserType().equals(sendUserType);
    }


    /*
     * 填充发送内容
     */
    @Override
    protected void populateSendContext(List<CouponSendContext> sendContexts) {
        //根据userid获取客户信息
        Set<String> customerIdSet = sendContexts.stream().filter(m -> m.getUserId() != null).map(m -> m.getUserId()).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(customerIdSet)) {
            List<CustomerIdInfoOutput> customers = listCustomerById(new ArrayList<>(customerIdSet));
            Map<Long, CustomerIdInfoOutput> customerMap = customers.stream().collect(Collectors.toMap(m -> m.getCustomerId(), m -> m));
            for (CouponSendContext sendContext : sendContexts) {
                CustomerIdInfoOutput customerInfo = customerMap.get(sendContext.getUserId());
                if (customerInfo == null) {
                    sendContext.error(false, "客户已注销或未注册");
                    continue;
                }
                sendContext.setBindTel(customerInfo.getPhone());
            }
            return;
        }

        //根据手机号获取客户信息
        Set<String> phoneNoSet = sendContexts.stream().filter(m -> m.getBindTel() != null).map(m -> m.getBindTel()).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(phoneNoSet)) {
            List<CustomerInfoSimpleOutput> customers = listCustomerByPhone(new ArrayList<>(phoneNoSet));
            Map<String, CustomerInfoSimpleOutput> customerMap = customers.stream().collect(Collectors.toMap(m -> m.getPhoneNo(), m -> m));
            for (CouponSendContext sendContext : sendContexts) {
                CustomerInfoSimpleOutput customerInfo = customerMap.get(sendContext.getBindTel());
                if (customerInfo == null || StringUtils.isBlank(customerInfo.getCustomerId())) {
                    sendContext.error(false, "客户已注销或未注册");
                    continue;
                }
                sendContext.setUserId(customerInfo.getCustomerId());
            }
            return;
        }

        throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_SEND_USER_EMPTY);
    }


    private List<CustomerInfoSimpleOutput> listCustomerByPhone(List<String> phoneNoList) {
        CustomerInfoSimpleInput input = new CustomerInfoSimpleInput();
        input.setPhoneNoList(phoneNoList);
        ResponseDto<List<CustomerInfoSimpleOutput>> responseDto = customerClient.listCustomerInfoByPhones(input);
        if (!CouponConstant.SUCCESS_CODE.equals(responseDto.getCode())) {
            throw new BusinessException(responseDto.getCode(), responseDto.getMessage());
        }
        return responseDto.getData();
    }

    private List<CustomerIdInfoOutput> listCustomerById(List<String> customerIds) {
        List<CustomerIdInfoOutput> customerIdInfoOutputs = new ArrayList<>();
        for (String customerId : customerIds) {
            CustomerIdInput customerIdInput = new CustomerIdInput();
            customerIdInput.setCustomerId(Long.valueOf(customerId));
            ResponseDto<CustomerIdInfoOutput> responseDto = customerClient.getCustomerInfoByCustomerId(customerIdInput);
            //用户信息不存在 未注册/已注销用户信息则不返回
            if ("D00016".equals(responseDto.getCode()) || responseDto.getData() == null) {
                continue;
            }
            if (!CouponConstant.SUCCESS_CODE.equals(responseDto.getCode())) {
                throw new BusinessException(responseDto.getCode(), responseDto.getMessage());
            }
            responseDto.getData().setCustomerId(customerIdInput.getCustomerId());
            customerIdInfoOutputs.add(responseDto.getData());
        }
        return customerIdInfoOutputs;
    }
}
