package com.fcb.coupon.app.controller;

import com.fcb.coupon.app.facade.ClientUserFacade;
import com.fcb.coupon.app.infra.inteceptor.AppUserInfo;
import com.fcb.coupon.app.model.bo.CouponThemeListBo;
import com.fcb.coupon.app.model.param.request.CouponThemeIdRequest;
import com.fcb.coupon.app.model.param.request.CouponThemeIdsRequest;
import com.fcb.coupon.app.model.param.request.CouponThemeListHouseRequest;
import com.fcb.coupon.app.model.param.response.CouponThemeResponse;
import com.fcb.coupon.app.service.CouponThemeService;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.ClientTypeEnum;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.util.CommonResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author mashiqiong
 * @date 2021-07-29 10:12
 */
@RestController
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Api(tags = {"优惠券活动Read接口"})
@Slf4j
public class CouponThemeController {
    private final CouponThemeService couponThemeService;
    private final ClientUserFacade clientUserFacade;


    /**
     * 查询优惠券活动列表及当前用户领券情况(注：此接口会直接对外)
     */
    @RequestMapping(value = "/inner-api/promotion/couponTheme/getCouponThemeList4app.do", consumes = "application/json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "查询优惠券活动列表及当前用户领券情况-营销活动页", httpMethod = "POST")
    public ResponseDto<List<CouponThemeResponse>> getCouponThemeList4app(@RequestBody @Validated CouponThemeIdsRequest request,
                                                                         HttpServletRequest httpServletRequest,
                                                                         @RequestHeader(name = "clientType", required = false) String clientType,
                                                                         @RequestHeader(name = "unionId", required = false) String unionId) {
        CouponThemeListBo bo = new CouponThemeListBo();
        bo.setIds(request.getIds());

        Integer userType = null;
        String userId = null;
        //检验登录
        if (ClientTypeEnum.B.getKey().equals(clientType)) {
            if (!clientUserFacade.validateMemberLogin(httpServletRequest)) {
                throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
            }
            AppUserInfo userInfo = clientUserFacade.getCustomerInfoByUnionId(unionId);
            userType = UserTypeEnum.B.getUserType();
            userId = userInfo.getUserId();
        } else if (ClientTypeEnum.C.getKey().equals(clientType)) {
            if (!clientUserFacade.validateCustomerLogin(httpServletRequest)) {
                throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
            }
            AppUserInfo userInfo = clientUserFacade.getMemberInfoByUnionId(unionId);
            userType = UserTypeEnum.C.getUserType();
            userId = userInfo.getUserId();
        }

        return CommonResponseUtil.successObj(couponThemeService.listByThemeIds(request.getIds(), userType, userId));
    }


    @RequestMapping(value = "/inner-api/promotion/coupon/getCouponThemeListByIds.do", consumes = "application/json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "查询优惠券活动列表及当前用户领券情况-楼盘/运营位", httpMethod = "POST")
    public ResponseDto<List<CouponThemeResponse>> getCouponThemeListHouse(@RequestBody @Validated CouponThemeListHouseRequest request,
                                                                          @RequestHeader(name = "clientType", required = false) String clientType) {
        Integer userType = null;
        String userId = null;
        //检验登录
        if (StringUtils.isNotBlank(request.getUserId()) && ClientTypeEnum.B.getKey().equals(clientType)) {
            AppUserInfo userInfo = clientUserFacade.getCustomerInfoByUnionId(request.getUserId());
            userType = UserTypeEnum.B.getUserType();
            userId = userInfo.getUserId();
        } else if (StringUtils.isNotBlank(request.getUserId()) && ClientTypeEnum.C.getKey().equals(clientType)) {
            AppUserInfo userInfo = clientUserFacade.getMemberInfoByUnionId(request.getUserId());
            userType = UserTypeEnum.C.getUserType();
            userId = userInfo.getUserId();
        }

        return CommonResponseUtil.successObj(couponThemeService.listByThemeIds(request.getIds(), userType, userId));
    }
}
