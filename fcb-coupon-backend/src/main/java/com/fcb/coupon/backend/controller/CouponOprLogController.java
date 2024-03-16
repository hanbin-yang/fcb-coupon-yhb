package com.fcb.coupon.backend.controller;

import com.fcb.coupon.backend.model.bo.CouponOprLogQueryBo;
import com.fcb.coupon.backend.model.param.request.CouponOprLogQueryRequest;
import com.fcb.coupon.backend.model.param.response.CouponOprLogResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.service.CouponOprLogService;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.backend.uitls.BackendResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 优惠券操作日志
 *
 * @Author WeiHaiQi
 * @Date 2021-06-23 17:01
 **/
@RestController
@RequestMapping(value="/couponOprLogRead")
@Api(tags = "优惠券操作日志")
public class CouponOprLogController {

    @Resource
    private CouponOprLogService couponOprLogService;

    @RequestMapping(value = "/getCouponOprLogListByPage.do", consumes = "application/json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "查询优惠券操作日志")
    public ResponseDto getCouponOprLogListByPage(@RequestBody CouponOprLogQueryRequest request) {
        CouponOprLogQueryBo bo = request.convert();
        PageResponse<CouponOprLogResponse> pageResponse = couponOprLogService.listByPageRequest(bo);
        return BackendResponseUtil.successObj(pageResponse);
    }
}
