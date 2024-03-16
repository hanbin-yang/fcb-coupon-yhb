package com.fcb.coupon.app.controller;

import com.fcb.coupon.app.business.CouponUserBusiness;
import com.fcb.coupon.app.exception.CouponErrorCode;
import com.fcb.coupon.app.infra.inteceptor.AppAuthorityHolder;
import com.fcb.coupon.app.infra.inteceptor.AppLogin;
import com.fcb.coupon.app.infra.inteceptor.AppUserInfo;
import com.fcb.coupon.app.model.PageDto;
import com.fcb.coupon.app.model.bo.CouponUserGetBo;
import com.fcb.coupon.app.model.bo.CouponUserListBo;
import com.fcb.coupon.app.model.dto.RealUserInfoDto;
import com.fcb.coupon.app.model.param.request.CouponUserGetRequest;
import com.fcb.coupon.app.model.param.request.QueryCouponRequest;
import com.fcb.coupon.app.model.param.response.CouponDetailResponse;
import com.fcb.coupon.app.model.param.response.CouponListResponse;
import com.fcb.coupon.app.model.param.response.CouponUserEffectiveTotalResponse;
import com.fcb.coupon.app.model.param.response.PageResponse;
import com.fcb.coupon.app.service.CouponUserService;
import com.fcb.coupon.app.service.UserFacadeService;
import com.fcb.coupon.common.constant.ClientTypeConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.CouponStatusEnum;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.CommonResponseUtil;
import com.fcb.coupon.common.util.DateUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * @author 唐陆军
 * @Description 用户优惠券
 * @createTime 2021年08月27日 17:42:00
 */
@RestController
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@RequestMapping
@Api(tags = {"用户的优惠券接口"})
@Slf4j
public class CouponUserController {

    private final CouponUserBusiness couponUserBusiness;
    private final UserFacadeService userFacadeService;
    private final CouponUserService couponUserService;

    @AppLogin
    @ApiOperation(value = "查询用户可用优惠券总数-B/C端直接调用")
    @ResponseBody
    @PostMapping(value = "/api/promotion/coupon/getUserCouponInfo.do")
    public ResponseDto<CouponUserEffectiveTotalResponse> getEffectiveCouponTotal(@RequestHeader(name = "clientType", required = false) String clientType) {
        AppUserInfo userInfo = AppAuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        CouponUserListBo couponUserListBo = new CouponUserListBo();
        couponUserListBo.setUserId(userInfo.getUserId());
        couponUserListBo.setUserType(userInfo.getUserType());
        couponUserListBo.setStatusList(Arrays.asList(CouponStatusEnum.STATUS_USE.getStatus(), CouponStatusEnum.STATUS_FREEZE.getStatus()));
        Integer total = couponUserService.countByEffective(couponUserListBo);
        CouponUserEffectiveTotalResponse response = new CouponUserEffectiveTotalResponse();
        response.setUserValidCoupons(total);
        return CommonResponseUtil.successObj(response);
    }


    @ApiOperation(value = "查询我的优惠券详情-B/C端给用户中心调用")
    @ResponseBody
    @PostMapping("/inner-api/promotion/coupon/getCouponById.do")
    public ResponseDto<CouponDetailResponse> getCouponById(@Valid @RequestBody CouponUserGetRequest request, @RequestHeader(name = "clientType", required = false) String clientType) {
        RealUserInfoDto realUserInfo = userFacadeService.getCacheRealUserInfo(clientType, request.getUserId());
        if (Objects.isNull(realUserInfo) || Objects.isNull(realUserInfo.getRealUserId())) {
            throw new BusinessException(CouponErrorCode.QUERY_COUPON_USER_NONE_EXCEPTION);
        }

        CouponUserGetBo bo = new CouponUserGetBo();
        bo.setUserId(realUserInfo.getRealUserId());
        bo.setUserType(realUserInfo.getUserType());
        bo.setCouponId(request.getCouponId());
        return CommonResponseUtil.newResult("0", "", couponUserBusiness.getByCouponId(bo));
    }

    @ResponseBody
    @ApiOperation(value = "查询我的优惠券列表-B/C端给用户中心调用")
    @RequestMapping(value = "/inner-api/promotion/coupon/getMyCouponList.do", consumes = "application/json", method = RequestMethod.POST)
    public ResponseDto<PageResponse<CouponListResponse>> getMyCouponList(@Valid @RequestBody QueryCouponRequest request, @RequestHeader(name = "clientType", required = false) String clientType) {
        RealUserInfoDto realUserInfo = userFacadeService.getCacheRealUserInfo(clientType, request.getUserId());
        if (Objects.isNull(realUserInfo) || Objects.isNull(realUserInfo.getRealUserId()) || Objects.isNull(realUserInfo.getMobilePhone())) {
            throw new BusinessException(CouponErrorCode.QUERY_COUPON_USER_NONE_EXCEPTION);
        }
        CouponUserListBo couponUserListBo = new CouponUserListBo();
        couponUserListBo.setUserId(realUserInfo.getRealUserId());
        couponUserListBo.setUserType(realUserInfo.getUserType());
        couponUserListBo.setStatusList(Arrays.asList(CouponStatusEnum.STATUS_USE.getStatus(), CouponStatusEnum.STATUS_FREEZE.getStatus()));
        couponUserListBo.setCouponDiscountType(Objects.equals(request.getCouponDiscountType(), 999) ? null : request.getCouponDiscountType());
        couponUserListBo.setSortedBy(request.getSortedBy());
        PageDto pageDto = new PageDto(request.getCurrentPage(), request.getItemsPerPage());
        return CommonResponseUtil.newResult("0", "", couponUserBusiness.listByEffective(couponUserListBo, pageDto));
    }

    @ResponseBody
    @ApiOperation(value = "查询我的失效优惠券列表-B/C端给用户中心调用")
    @RequestMapping(value = "/inner-api/promotion/coupon/getMyExpiredCouponList.do", consumes = "application/json", method = RequestMethod.POST)
    public ResponseDto<PageResponse<CouponListResponse>> getMyExpiredCouponList(@Valid @RequestBody QueryCouponRequest request, @RequestHeader(name = "clientType", required = false) String clientType) {
        RealUserInfoDto realUserInfo = userFacadeService.getCacheRealUserInfo(clientType, request.getUserId());
        if (Objects.isNull(realUserInfo) || Objects.isNull(realUserInfo.getRealUserId())) {
            throw new BusinessException(CouponErrorCode.QUERY_COUPON_USER_NONE_EXCEPTION);
        }

        CouponUserListBo couponUserListBo = new CouponUserListBo();
        couponUserListBo.setUserId(realUserInfo.getRealUserId());
        couponUserListBo.setUserType(realUserInfo.getUserType());
        couponUserListBo.setStatusList(Arrays.asList(CouponStatusEnum.STATUS_USED.getStatus(),
                CouponStatusEnum.STATUS_CANCEL.getStatus(),
                CouponStatusEnum.STATUS_DONATE.getStatus(),
                CouponStatusEnum.STATUS_ASSIGN.getStatus(),
                CouponStatusEnum.STATUS_LOCKED.getStatus()));
        couponUserListBo.setCouponDiscountType(Objects.equals(request.getCouponDiscountType(), 999) ? null : request.getCouponDiscountType());
        couponUserListBo.setSortedBy(request.getSortedBy() == null ? 0 : request.getSortedBy());
        //查询过期三个月以内的
        couponUserListBo.setEndTime(DateUtils.getDelayMonth(new Date(), -3));
        PageDto pageDto = new PageDto(request.getCurrentPage(), request.getItemsPerPage());
        return CommonResponseUtil.newResult("0", "", couponUserBusiness.listByExpired(couponUserListBo, pageDto));
    }


    @AppLogin(clientType = ClientTypeConstant.SAAS)
    @ApiOperation(value = "查询我的优惠券详情-Saas端服务调用")
    @ResponseBody
    @PostMapping("/api/promotion/coupon/getCouponById.do")
    public ResponseDto<CouponDetailResponse> getSaasCouponById(@RequestBody CouponUserGetRequest request) {
        CouponUserGetBo bo = new CouponUserGetBo();
        bo.setUserId(request.getUserId());
        bo.setUserType(UserTypeEnum.SAAS.getUserType());
        bo.setCouponId(request.getCouponId());
        return CommonResponseUtil.newResult("0", "", couponUserBusiness.getByCouponId(bo));
    }


    @AppLogin(clientType = ClientTypeConstant.SAAS)
    @ResponseBody
    @ApiOperation(value = "查询我的优惠券列表-Saas端服务调用")
    @RequestMapping(value = "/api/promotion/coupon/getMyCouponList.do", consumes = "application/json", method = RequestMethod.POST)
    public ResponseDto<PageResponse> getSaasMyCouponList(@RequestBody QueryCouponRequest request) {
        CouponUserListBo couponUserListBo = new CouponUserListBo();
        couponUserListBo.setUserId(request.getUserId());
        couponUserListBo.setUserType(UserTypeEnum.SAAS.getUserType());
        couponUserListBo.setStatusList(Arrays.asList(CouponStatusEnum.STATUS_USE.getStatus(), CouponStatusEnum.STATUS_FREEZE.getStatus()));
        couponUserListBo.setCouponDiscountType(request.getCouponDiscountType());
        couponUserListBo.setSortedBy(request.getSortedBy());
        PageDto pageDto = new PageDto(request.getCurrentPage(), request.getItemsPerPage());
        return CommonResponseUtil.newResult("0", "", couponUserBusiness.listByEffective(couponUserListBo, pageDto));
    }

    @AppLogin(clientType = ClientTypeConstant.SAAS)
    @ResponseBody
    @ApiOperation(value = "查询我的失效优惠券列表-Saas端服务调用")
    @RequestMapping(value = "/api/promotion/coupon/getMyExpiredCouponList.do", consumes = "application/json", method = RequestMethod.POST)
    public ResponseDto<PageResponse> getSaasMyExpiredCouponList(@RequestBody QueryCouponRequest request) {
        CouponUserListBo couponUserListBo = new CouponUserListBo();
        couponUserListBo.setUserId(request.getUserId());
        couponUserListBo.setUserType(UserTypeEnum.SAAS.getUserType());
        couponUserListBo.setStatusList(Arrays.asList(CouponStatusEnum.STATUS_USED.getStatus(),
                CouponStatusEnum.STATUS_CANCEL.getStatus(),
                CouponStatusEnum.STATUS_DONATE.getStatus(),
                CouponStatusEnum.STATUS_ASSIGN.getStatus(),
                CouponStatusEnum.STATUS_LOCKED.getStatus()));
        couponUserListBo.setCouponDiscountType(Objects.equals(request.getCouponDiscountType(), 999) ? null : request.getCouponDiscountType());
        couponUserListBo.setSortedBy(request.getSortedBy() == null ? 0 : request.getSortedBy());
        //查询过期三个月以内的
        couponUserListBo.setEndTime(DateUtils.getDelayMonth(new Date(), -3));
        PageDto pageDto = new PageDto(request.getCurrentPage(), request.getItemsPerPage());
        return CommonResponseUtil.newResult("0", "", couponUserBusiness.listByExpired(couponUserListBo, pageDto));
    }


}
