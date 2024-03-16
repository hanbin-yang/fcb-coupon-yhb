package com.fcb.coupon.app.controller;

import com.fcb.coupon.app.model.param.response.*;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.ClientTypeEnum;
import com.fcb.coupon.common.enums.CouponDiscountType;
import com.fcb.coupon.common.util.CommonResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 优惠券查询服务api
 *
 * @Author WeiHaiQi
 * @Date 2021-08-16 11:54
 **/
@RestController
@Api(tags = "优惠券查询服务api")
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponController {


    @ApiOperation(value = "优惠券分类列表")
    @ResponseBody
    @PostMapping("/inner-api/promotion/coupon/getCouponDiscountTypeList.do")
    public ResponseDto<List<CouponDiscountTypeResponse>> getCouponDiscountTypeList(@RequestHeader(name = "clientType", required = false) String clientType) {
        List<CouponDiscountTypeResponse> couponDiscountTypeResponses = new ArrayList<>();
        for (CouponDiscountType couponDiscountType : CouponDiscountType.values()) {
            if (Objects.equals(clientType, ClientTypeEnum.C.getKey()) && Objects.equals(couponDiscountType.getType(), CouponDiscountType.RED_ENVELOP.getType())) {
                continue;
            }
            CouponDiscountTypeResponse vo = new CouponDiscountTypeResponse();
            vo.setCode(couponDiscountType.getType());
            vo.setValue(couponDiscountType.getDesc());
            couponDiscountTypeResponses.add(vo);
        }
        return CommonResponseUtil.newResult("0", "", couponDiscountTypeResponses);
    }



}
