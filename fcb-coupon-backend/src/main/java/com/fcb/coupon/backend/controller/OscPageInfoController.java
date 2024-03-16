package com.fcb.coupon.backend.controller;

import com.alibaba.fastjson.JSONObject;
import com.fcb.coupon.backend.exception.OscPageInfoErrorCode;
import com.fcb.coupon.backend.infra.inteceptor.IgnoreAuthorityPath;
import com.fcb.coupon.backend.service.OscPageInfoService;
import com.fcb.coupon.backend.uitls.BackendResponseUtil;
import com.fcb.coupon.common.dto.ResponseDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author YangHanBin
 * @date 2021-06-18 10:11
 */
@RestController
@RequestMapping("/oscRead")
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Api(tags = {"pageInfo页面字典信息"})
@IgnoreAuthorityPath
public class OscPageInfoController {
    private final OscPageInfoService oscPageInfoService;

    @PostMapping(value = "/loadOscConfig.action")
    @ResponseBody
    @ApiOperation(value = "加载页面项", httpMethod = "POST")
    public ResponseDto<JSONObject> loadOscConfig(@RequestBody String configKey){
        JSONObject jsonObject = oscPageInfoService.getJsonObject(configKey);
        return BackendResponseUtil.successObj(jsonObject);
    }

    @PostMapping(value = "/loadCouponPageConfig.action")
    @ResponseBody
    @ApiOperation(value = "加载优惠券页面项", httpMethod = "POST")
    public ResponseDto<JSONObject> loadCouponPageConfig(@RequestBody String pageConfigRequestVO) {
        JSONObject jsonObject = oscPageInfoService.childJsonObject(pageConfigRequestVO);
        if (jsonObject != null) {
            return BackendResponseUtil.successObj(jsonObject);
        } else {
            return BackendResponseUtil.fail(OscPageInfoErrorCode.LOAD_FAIL);
        }
    }

    @RequestMapping(value = "loadPageConfigCommonOsc.action", consumes = "application/json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "加载common页面项", httpMethod = "POST")
    public Object loadPageConfigCommonOsc(@RequestBody String configKey) {
        JSONObject jsonObject = oscPageInfoService.childJsonObject(configKey);
        if (jsonObject != null) {
            return BackendResponseUtil.successObj(jsonObject);
        } else {
            return BackendResponseUtil.fail(OscPageInfoErrorCode.LOAD_FAIL);
        }
    }

    @RequestMapping(value = "loadPageConfig.action", consumes = "application/json", method = RequestMethod.POST)
    @ResponseBody
    public Object loadPageConfig() {

        JSONObject jsonObject = oscPageInfoService.childJsonObject("PAGE_CONFIG");
        if (jsonObject != null) {
            return BackendResponseUtil.successObj(jsonObject);
        } else {
            return BackendResponseUtil.fail(OscPageInfoErrorCode.LOAD_FAIL);
        }
    }
}
