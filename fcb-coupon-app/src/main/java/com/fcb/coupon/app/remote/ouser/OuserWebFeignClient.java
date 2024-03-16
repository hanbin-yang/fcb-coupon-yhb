package com.fcb.coupon.app.remote.ouser;

import com.fcb.coupon.app.remote.dto.input.InputDto;
import com.fcb.coupon.app.remote.dto.input.OrgInfoDto;
import com.fcb.coupon.app.remote.dto.output.OutputDto;
import com.fcb.coupon.app.remote.dto.output.StoreInfoOutDto;
import com.fcb.coupon.app.remote.dto.output.StoreOrgInfoOutDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(value = "ouser-web",url = "${remote.url.middleend.adminportal}")
public interface OuserWebFeignClient {

    @PostMapping(value = "/ouser-web/cloud/storeService/queryAllStore4Coupon")
    OutputDto<List<StoreOrgInfoOutDto>> queryAllStore4Coupon(InputDto<OrgInfoDto> inputDTO);

    @PostMapping(value = "/ouser-web/cloud/storeService/queryAllStore")
    OutputDto<List<StoreInfoOutDto>> queryAllStore(InputDto<OrgInfoDto> inputDto);
}
