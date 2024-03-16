package com.fcb.coupon.app.controller;

import com.fcb.coupon.app.business.couponreceive.CouponReceiveBusiness;
import com.fcb.coupon.app.exception.CouponReceiveErrorCode;
import com.fcb.coupon.app.facade.ClientUserFacade;
import com.fcb.coupon.app.infra.inteceptor.AppUserInfo;
import com.fcb.coupon.app.model.bo.CouponReceiveBo;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.param.request.*;
import com.fcb.coupon.app.model.param.response.CouponThemeResponse;
import com.fcb.coupon.app.service.CouponThemeService;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.ClientTypeEnum;
import com.fcb.coupon.common.enums.CouponGiveRuleEnum;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.util.CommonResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-08-18 8:36
 */
@RestController
@Api(tags = {"微吼直播相关"})
@RequestMapping
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class WeiHouController {
    private final ClientUserFacade clientUserFacade;
    private final CouponReceiveBusiness couponReceiveBusiness;
    private final CouponThemeService couponThemeService;

    /**
     * 微吼查询优惠券活动
     */
    @PostMapping(value = "/inner-api/promotion/couponTheme/getCouponThemeInfo.do", consumes = "application/json")
    @ApiOperation(value = "微吼查询优惠券活动", httpMethod = "POST")
    public ResponseDto<CouponThemeResponse> getCouponThemeInfo(@RequestBody @Validated CouponThemeIdRequest request) {
        return CommonResponseUtil.successObj(couponThemeService.getByThemeId(request.getId()));
    }

    /**
     * 微吼查询优惠券活动列表（后台）
     */
    @PostMapping(value = "/inner-api/promotion/couponTheme/getCouponThemeList.do", consumes = "application/json")
    @ApiOperation(value = "优惠券活动列表", httpMethod = "POST")
    public ResponseDto<List<CouponThemeResponse>> getCouponThemeList(@RequestBody @Validated CouponThemeIdsRequest request) {
        return CommonResponseUtil.successObj(couponThemeService.listByThemeIds(request.getIds()));
    }


    @PostMapping(value = "/inner-api/promotion/coupon/receiveCoupon4weihou.do", produces = {"application/json;charset=UTF-8"})
    @ApiOperation(value = "微吼直播--领券", httpMethod = "POST")
    public ResponseDto<Void> receiveCoupon4weiHou(@RequestBody ReceiveCoupon4WeiHouRequest in,
                                                  @RequestHeader(name = "clientType") String clientType,
                                                  @RequestHeader(name = "hdToken") String hdToken,
                                                  @RequestHeader(name = "terminalType") String terminalType) {
        CouponReceiveBo bo = in.convert();
        switch (ClientTypeEnum.of(clientType)) {
            case B:
                AppUserInfo bUserInfo = clientUserFacade.getMemberInfoByHdTokenAndTerminalType(hdToken, terminalType);
                bo.setUserId(bUserInfo.getUserId());
                bo.setUserMobile(bUserInfo.getUserMobile());
                bo.setUserType(UserTypeEnum.B.getUserType());
                break;
            case C:
                AppUserInfo cUserInfo = clientUserFacade.getCustomerInfoByHdTokenAndTerminalType(hdToken, terminalType);
                bo.setUserId(cUserInfo.getUserId());
                bo.setUserMobile(cUserInfo.getUserMobile());
                bo.setUserType(UserTypeEnum.C.getUserType());
                break;
            default:
                throw new BusinessException(CommonErrorCode.USER_TYPE_UNSUPPORTED);
        }
        CouponThemeCache couponThemeCache = couponReceiveBusiness.getCouponThemeCache(bo.getCouponThemeId());
        if (!CouponGiveRuleEnum.COUPON_GIVE_RULE_LIVE.ifSame(couponThemeCache.getCouponGiveRule())) {
            log.error("微吼直播--领券 error: couponGiveRule={}", couponThemeCache.getCouponGiveRule());
            CouponReceiveErrorCode errorCode = CouponReceiveErrorCode.COUPON_GIVE_RULE_ILLEGAL;
            String realMessage = String.format(errorCode.getMessage(), CouponGiveRuleEnum.COUPON_GIVE_RULE_LIVE.getTypeStr());
            errorCode.setMessage(realMessage);
            throw new BusinessException(errorCode);
        }
        couponReceiveBusiness.receive(bo, couponThemeCache);

        return CommonResponseUtil.success();
    }
}


