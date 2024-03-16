package com.fcb.coupon.backend.remote.client;

import com.fcb.coupon.backend.remote.dto.input.CustomerIdInput;
import com.fcb.coupon.backend.remote.dto.input.CustomerInfoSimpleInput;
import com.fcb.coupon.backend.remote.dto.out.CustomerIdInfoOutput;
import com.fcb.coupon.backend.remote.dto.out.CustomerInfoSimpleOutput;
import com.fcb.coupon.common.constant.RestConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "customer", url = "${remote.url.customer.domain}")
public interface CustomerClient {

    /**
     * 根据电话号码批量获取C端用户信息
     */
    @PostMapping(value = RestConstant.GET_REGISTER_USER_INFO_LIST_URL)
    ResponseDto<List<CustomerInfoSimpleOutput>> listCustomerInfoByPhones(@RequestBody CustomerInfoSimpleInput param);

    @PostMapping(value = RestConstant.GET_CUSTOMER_INFO_BY_CUSTOMERID_URL)
    ResponseDto<CustomerIdInfoOutput> getCustomerInfoByCustomerId(@RequestBody CustomerIdInput param);

}
