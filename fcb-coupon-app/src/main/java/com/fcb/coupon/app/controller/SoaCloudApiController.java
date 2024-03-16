package com.fcb.coupon.app.controller;

import com.fcb.coupon.app.business.CouponUserBusiness;
import com.fcb.coupon.app.model.bo.CouponThemeListBo;
import com.fcb.coupon.app.model.param.request.CouponThemeListRequest;
import com.fcb.coupon.app.model.param.request.InputRequest;
import com.fcb.coupon.app.model.param.request.QueryUserCouponRequest;
import com.fcb.coupon.app.model.param.response.CouponThemeResponse;
import com.fcb.coupon.app.model.param.response.OutputResponse;
import com.fcb.coupon.app.model.param.response.CouponUserEffectiveSoaResponse;
import com.fcb.coupon.app.model.param.response.PageResponse;
import com.fcb.coupon.app.service.CouponThemeService;
import com.fcb.coupon.app.uitls.SoaUtil;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.util.CommonResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;


/**
 * 旧SOA重构接口
 *
 * @Author WeiHaiQi
 * @Date 2021-08-12 18:42
 **/
@RestController
@RequestMapping(value = "/cloud")
@Api(tags = "SOA重构接口")
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class SoaCloudApiController {

    private final CouponUserBusiness couponUserBusiness;
    private final CouponThemeService couponThemeService;



}
