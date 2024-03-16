package com.fcb.coupon.app.remote.activity;

import com.fcb.coupon.app.remote.dto.input.PromotionCheckResInput;
import com.fcb.coupon.app.remote.dto.output.PromotionCheckResOutput;
import com.fcb.coupon.common.constant.RestConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * fcb-activity-service活动接口
 *
 * @Author WeiHaiQi
 * @Date 2021-08-13 14:32
 **/
@FeignClient(value = "fcb-activity-service", url = "http://10.71.176.79:31744/")
public interface FcbActivityFeignClient {

    @PostMapping(value = RestConstant.QUERY_ACTIVITY_INFO_BY_CHECK)
    ResponseDto<PromotionCheckResOutput> queryActivityInfoByCheck(@RequestBody PromotionCheckResInput in, @RequestHeader(name = "clientType") String clientType);
}
