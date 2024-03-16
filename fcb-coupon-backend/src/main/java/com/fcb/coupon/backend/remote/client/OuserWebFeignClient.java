package com.fcb.coupon.backend.remote.client;

import com.fcb.coupon.backend.model.ao.OrgRangeAo;
import com.fcb.coupon.backend.model.dto.OrgInfoByPluralismInDto;
import com.fcb.coupon.backend.model.dto.OrgInfoByPluralismOutDto;
import com.fcb.coupon.backend.model.dto.StoreInfoInputDto;
import com.fcb.coupon.backend.model.dto.StoreInfoOutDto;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.remote.dto.input.InputDto;
import com.fcb.coupon.backend.remote.dto.input.OrgIdsInput;
import com.fcb.coupon.backend.remote.dto.input.OrgInfoDto;
import com.fcb.coupon.backend.remote.dto.out.OrgOutDto;
import com.fcb.coupon.backend.remote.dto.out.OutputDataDto;
import com.fcb.coupon.backend.remote.dto.out.OutputDto;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author HanBin_Yang
 * @since 2021/6/27 20:55
 */
@FeignClient(value = "ouser-web",url = "${remote.url.middleend.adminportal}")
public interface OuserWebFeignClient {

    @PostMapping(value = "/ouser-web/cloud/storeService/queryAllStore")
    OutputDto<List<StoreInfoOutDto>> queryAllStore(InputDto<OrgInfoDto> inputDto);

    @PostMapping(value = "/ouser-web/cloud/storeService/queryStoreOrgPageByParams")
    OutputDto<PageResponse<StoreInfoOutDto>> queryStoreOrgPageByParams(InputDto<StoreInfoInputDto> inputDto);

    @PostMapping(value = "/ouser-web/cloud/storeService/getOrgInfoListByPluralism")
    OutputDto<List<OrgInfoByPluralismOutDto>> getOrgInfoListByPluralism(InputDto<OrgInfoByPluralismInDto> inputDto);

    @PostMapping(value = "/ouser-web/api/merchant/queryMerchantTree.do")
    @Headers(value = {"Content-Type: application/json", "Cookie: {cookie}"})
    OutputDto<OutputDataDto<OrgRangeAo>> queryMerchantTree(@RequestBody Map<String, Object> inputDto, @RequestHeader("cookie") String ut);

//    @PostMapping(value = "/ouser-web/cloud/orgInfoNameService/findByOrgId")
//    OutputDto<List<CouponThemeOrgInfoAo>> findByOrgId(InputDto<Map<String, Object>> inputDto);

    /**
     * 根据条件查询组织信息
     */
    @RequestMapping(value = "/ouser-web/cloud/orgInfoNameService/findByOrgId", method = RequestMethod.POST)
    OutputDto<List<OrgOutDto>> findByOrgId(InputDto<OrgIdsInput> inputDTO);

}
