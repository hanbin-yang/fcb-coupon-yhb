package com.fcb.coupon.app.controller;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.app.business.CouponGiveBusiness;
import com.fcb.coupon.app.exception.CouponGiveErrorCode;
import com.fcb.coupon.app.facade.ClientUserFacade;
import com.fcb.coupon.app.infra.inteceptor.AppAuthorityHolder;
import com.fcb.coupon.app.infra.inteceptor.AppLogin;
import com.fcb.coupon.app.infra.inteceptor.AppUserInfo;
import com.fcb.coupon.app.model.bo.CouponBeforeGiveAddBo;
import com.fcb.coupon.app.model.bo.ReceiveBeforeGivingBo;
import com.fcb.coupon.app.model.param.request.CouponBeforeGiveAddRequest;
import com.fcb.coupon.app.model.param.request.CouponBeforeGiveGetSmsCountRequest;
import com.fcb.coupon.app.model.param.request.CouponGiveReceiveRequest;
import com.fcb.coupon.app.model.param.request.ReceiveBeforeGivingRequest;
import com.fcb.coupon.app.model.param.response.CouponBeforeGiveAddResponse;
import com.fcb.coupon.app.model.param.response.ReceiveBeforeGivingResponse;
import com.fcb.coupon.app.remote.user.SaasFeignClient;
import com.fcb.coupon.app.service.CouponBeforeGiveService;
import com.fcb.coupon.app.service.CouponService;
import com.fcb.coupon.app.uitls.NumberConvertUtils;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.RedisLockKeyConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.BusinessTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.util.CommonResponseUtil;
import com.fcb.coupon.common.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author YangHanBin
 * @date 2021-08-13 8:53
 */
@RestController
@RequestMapping
@Api(tags = {"优惠券转赠相关"})
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponGiveController {
    private final CouponBeforeGiveService couponBeforeGiveService;
    private final ClientUserFacade clientUserFacade;
    private final SaasFeignClient saasFeignClient;
    private final CouponGiveBusiness couponGiveBusiness;
    private final CouponService couponService;


    @PostMapping(value = "/api/promotion/coupon/getCouponBeforeGivingInfo")
    @ApiOperation(value = "转增--领取福利", httpMethod = "POST")
    public ResponseDto<ReceiveBeforeGivingResponse> getCouponBeforeGivingInfo(@RequestBody @Valid ReceiveBeforeGivingRequest in) {
        log.info("转增--领取福利getCouponBeforeGivingInfo start: in={}", JSON.toJSONString(in));
        ReceiveBeforeGivingBo bo = in.convert();

        Long beforeGiveId = NumberConvertUtils.shortStrConvertToNumber(in.getBeforeGiveCode());
        bo.setCouponBeforeGiveId(beforeGiveId);

        ReceiveBeforeGivingResponse out = couponBeforeGiveService.getCouponBeforeGivingInfo(bo);
        log.info("转增--领取福利getCouponBeforeGivingInfo end: out={}", JSON.toJSONString(out));

        if (Objects.isNull(out)) {
            ReceiveBeforeGivingResponse response = new ReceiveBeforeGivingResponse();
            response.setContent("来晚了，领取已失效");
            return CommonResponseUtil.success(CouponConstant.FAIL_MESSAGE, response);
        } else if (out.getExpireTime().before(new Date())) {
            return CommonResponseUtil.success(CouponConstant.FAIL_MESSAGE, out);
        }

        return CommonResponseUtil.successObj(out);
    }

    /**
     * 添加转赠前的优惠券信息 - B
     */
    @AppLogin()
    @ApiOperation(value = "添加转赠前的优惠券信息")
    @PostMapping(value = "/inner-api/promotion/coupon/addCouponBeforeGiveById.do")
    public ResponseDto<CouponBeforeGiveAddResponse> addCouponBeforeGiveById(@RequestBody CouponBeforeGiveAddRequest in,
                                                                            @RequestHeader(name = "Authorization") String userToken,
                                                                            @RequestHeader(name = "clientType") String clientType,
                                                                            @RequestHeader(name = "terminalType", required = false) String terminalType) throws Exception {

        CouponBeforeGiveAddBo bo = in.convert();
        bo.setUserToken(userToken);
        bo.setClientType(clientType);
        bo.setTerminalType(terminalType);
        bo.setOprationType(BusinessTypeEnum.COUPON_GIVE.getType().toString());
        CouponBeforeGiveAddResponse couponBeforeGiveAddResponse = couponBeforeGiveService.addCouponBeforeGiveById(bo);
        return CommonResponseUtil.newResult("0", "成功", couponBeforeGiveAddResponse);

    }

    /**
     * 根据券id统计转赠前的优惠券信息次数 (短信赠送) - B
     */
    @AppLogin
    @ApiOperation(value = "根据券id统计转赠前的优惠券信息次数 (短信赠送)")
    @PostMapping(value = "/inner-api/promotion/coupon/getCouponBeforeGiveCanSendSmsCount.do")
    public ResponseDto<Map<String, Integer>> getCouponBeforeGiveCanSendSmsCount(@RequestBody CouponBeforeGiveGetSmsCountRequest in,
                                                                                @RequestHeader(name = "Authorization") String userToken,
                                                                                @RequestHeader(name = "clientType") String clientType,
                                                                                @RequestHeader(name = "terminalType", required = false) String terminalType, HttpServletRequest request) throws Exception {

        Integer count = couponBeforeGiveService.getCouponBeforeGiveCanSendSmsCount(in.getCouponId());
        Map<String, Integer> map = new HashedMap<>();
        map.put("count", count);
        return CommonResponseUtil.newResult("0", "成功", map);
    }

    /**
     * 添加转赠前的优惠券信息 - Saas
     */
    @AppLogin()
    @ApiOperation(value = "添加转赠前的优惠券信息")
    @PostMapping(value = "/api/promotion/coupon/addCouponBeforeGiveById.do")
    public ResponseDto<CouponBeforeGiveAddResponse> addCouponBeforeGiveByIdSaas(@RequestBody CouponBeforeGiveAddRequest in,
                                                                                @RequestHeader(name = "Authorization") String userToken,
                                                                                @RequestHeader(name = "clientType") String clientType,
                                                                                @RequestHeader(name = "terminalType", required = false) String terminalType) throws Exception {

        CouponBeforeGiveAddBo bo = in.convert();
        bo.setUserToken(userToken);
        bo.setClientType(clientType);
        bo.setTerminalType(terminalType);
        bo.setOprationType(BusinessTypeEnum.COUPON_GIVE.getType().toString());
        CouponBeforeGiveAddResponse couponBeforeGiveAddResponse = couponBeforeGiveService.addCouponBeforeGiveById(bo);
        return CommonResponseUtil.newResult("0", "成功", couponBeforeGiveAddResponse);

    }

    /**
     * 根据券id统计转赠前的优惠券信息次数 (短信赠送) - Saas
     */
    @AppLogin
    @ApiOperation(value = "根据券id统计转赠前的优惠券信息次数 (短信赠送)")
    @PostMapping(value = "/api/promotion/coupon/getCouponBeforeGiveCanSendSmsCount.do")
    public ResponseDto<Map<String, Integer>> getCouponBeforeGiveCanSendSmsCountSaas(@RequestBody CouponBeforeGiveGetSmsCountRequest in,
                                                                                    @RequestHeader(name = "Authorization") String userToken,
                                                                                    @RequestHeader(name = "clientType") String clientType,
                                                                                    @RequestHeader(name = "terminalType", required = false) String terminalType, HttpServletRequest request) throws Exception {
        Integer count = couponBeforeGiveService.getCouponBeforeGiveCanSendSmsCount(in.getCouponId());
        Map<String, Integer> map = new HashedMap<>();
        map.put("count", count);
        return CommonResponseUtil.newResult("0", "成功", map);
    }


    @AppLogin
    @PostMapping(value = "/api/promotion/coupon/receiveGiveCoupon")
    @ApiOperation(value = "领券转赠优惠券-C端使用", httpMethod = "POST")
    public ResponseDto<ReceiveBeforeGivingResponse> receiveGiveCoupon(@RequestBody @Valid CouponGiveReceiveRequest request) {
        Long beforeGiveId = null;
        try {
            beforeGiveId = NumberConvertUtils.shortStrConvertToNumber(request.getBeforeGiveCode());
        } catch (NumberFormatException ex) {
            throw new BusinessException(CommonErrorCode.PARAMS_ERROR.getCode(), "转赠记录编码解析错误");
        }
        AppUserInfo userInfo = AppAuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        //限制5s的访问频率
        String lockKey = RedisLockKeyConstant.getCouponGiveLockKey(beforeGiveId, userInfo.getUserId());
        if (!RedisUtil.tryLock(lockKey, "", 5)) {
            throw new BusinessException(CouponGiveErrorCode.GIVE_COUPON_BUSY_ERROR);
        }
        couponGiveBusiness.receive(beforeGiveId, userInfo.getUserId());
        return CommonResponseUtil.success("恭喜您，领取成功", null);
    }


}
