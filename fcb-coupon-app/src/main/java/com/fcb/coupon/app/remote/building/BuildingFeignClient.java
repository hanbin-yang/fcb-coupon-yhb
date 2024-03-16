package com.fcb.coupon.app.remote.building;

import com.fcb.coupon.app.remote.dto.input.BuildingListByItemIdInput;
import com.fcb.coupon.app.remote.dto.output.StoreInfoOutput;
import com.fcb.coupon.common.constant.RestConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-08-24 14:03
 */
@FeignClient(value = "hdb-prod-external-api", url = "http://10.71.176.79:31306/")
public interface BuildingFeignClient {

    @PostMapping(value = RestConstant.QUERY_PRODUCT_INFO)
    ResponseDto<List<StoreInfoOutput>> queryBuildingInfoByItemId(@RequestBody BuildingListByItemIdInput in);

}
