package com.fcb.coupon.backend.remote.client;

import com.fcb.coupon.backend.remote.dto.out.UserActivityDto;
import com.fcb.coupon.common.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * crm-web组件服务
 *
 * @Author WeiHaiQi
 * @Date 2021-06-21 23:03
 **/
@FeignClient(value = "crm-web",url = "${remote.url.middleend.adminportal}")
public interface CrmWebService {

    @PostMapping(value = "/crm-web/api/userInfo/queryOngoingActivityList.do?companyId=180",consumes = "application/json;charset=UTF-8")
    ResponseDto<List<UserActivityDto>> queryOngoingActivityList(@RequestBody List<String> unionIdList);
}
