package com.fcb.coupon.backend.controller;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.backend.infra.inteceptor.IgnoreAuthorityPath;
import com.fcb.coupon.backend.infra.inteceptor.IgnoreLogin;
import com.fcb.coupon.backend.model.bo.*;
import com.fcb.coupon.backend.model.param.request.*;
import com.fcb.coupon.backend.model.param.response.CouponViewResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.service.CouponBeforeGiveCacheService;
import com.fcb.coupon.backend.service.CouponEsManageService;
import com.fcb.coupon.backend.service.CouponService;
import com.fcb.coupon.common.constant.RedisCacheKeyConstant;
import com.fcb.coupon.common.dto.RedisLockResult;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.CouponRefreshEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.backend.uitls.BackendResponseUtil;
import com.fcb.coupon.common.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 优惠券Write接口
 *
 * @Author WeiHaiQi
 * @Date 2021-06-18 22:11
 **/
@RestController
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Api(tags = "优惠券接口")
@Slf4j
public class CouponController {
    private final CouponService couponService;
    private final CouponEsManageService couponEsManageService;
    private final CouponBeforeGiveCacheService couponBeforeGiveCacheService;

    @RequestMapping(value = "/couponWrite/invalidCoupon.do", consumes = "application/json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "作废券明细")
    @IgnoreAuthorityPath
    public ResponseDto<Void> invalidCoupon(@RequestBody @Validated CouponInvalidRequest couponInvalidRequest) {
        CouponInvalidBo bo = couponInvalidRequest.convert();
        couponService.invalidCouponWithTx(bo);
        return BackendResponseUtil.success();
    }

    @RequestMapping(value = "/couponWrite/freezeCoupon.do", consumes = "application/json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "冻结/解冻优惠券")
    @IgnoreAuthorityPath
    public ResponseDto<Void> freezeCoupon(@RequestBody @Validated FreezeCouponRequest requestParam) {
        FreezeCouponBo bo = requestParam.convert();
        couponService.freezeCouponWithTx(bo);
        return BackendResponseUtil.success();
    }

    @RequestMapping(value = "/couponWrite/postponeCoupon.do", consumes = "application/json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "优惠券延期")
    @IgnoreAuthorityPath
    public ResponseDto<Void> postponeCoupon(@RequestBody PostponeCouponRequest request) {
        PostponeCouponBo bo = request.convert();
        couponService.postponeCouponWithTx(bo);
        return BackendResponseUtil.success();
    }

    @ResponseBody
    @PostMapping(value = "/couponWrite/refreshCouponToEs.do", consumes = "application/json")
    @ApiOperation(value = "条件刷新(支持单个券活动，单个券主键id), coupon表数据刷新到elasticsearch")
    @IgnoreAuthorityPath
    public ResponseDto<String> refreshElasticsearch(@RequestBody @Validated CouponRefreshToEsRequest param) {
        log.info("coupon表数据刷新到elasticsearch start: ao={}", JSON.toJSONString(param));
        // 入参校验
        if (CouponRefreshEnum.BY_COUPON_ID.getFlag().equals(param.getRefreshType()) && Objects.isNull(param.getCouponId())) {
            throw new BusinessException(CommonErrorCode.API_CALL_ERROR.getCode(), "couponId字段不能为空");
        }

        StringBuilder keyName = new StringBuilder().append(RedisCacheKeyConstant.COUPON_REFRESH_TO_ES).append(param.getCouponThemeId());
        if (Objects.nonNull(param.getCouponId())) {
            keyName.append(":couponId:").append(param.getCouponId());
        }

        RedisLockResult<Long> redisLockResult;
        if (CouponRefreshEnum.BY_COUPON_ALL.getFlag().equals(param.getRefreshType())) {
            keyName.append(CouponRefreshEnum.BY_COUPON_ALL.getFlag());
            redisLockResult = RedisUtil.executeTryLock(keyName.toString(), 0, 12, TimeUnit.HOURS, () -> {
                couponEsManageService.syncAllCoupon();
                return null;
            });
        } else {
            redisLockResult = RedisUtil.executeTryLock(keyName.toString(), 0, 12, TimeUnit.HOURS, () -> couponEsManageService.refreshEsByCouponThemeId(param.getCouponThemeId()));
        }

        if (redisLockResult.isFailure()) {
            return BackendResponseUtil.fail(CommonErrorCode.API_CALL_ERROR.getCode(), "数据刷新中，请勿重复操作！");
        }

        return BackendResponseUtil.success("成功刷新ES优惠券数据", null);
    }

    @ResponseBody
    @PostMapping(value = "/couponWrite/refreshCouponBeforeGive", consumes = "application/json")
    @ApiOperation(value = "刷新转增前记录")
    @IgnoreLogin
    public ResponseDto<String> refreshCouponBeforeGive(@RequestBody RefreshCouponBeforeGiveCacheRequest in) {
        couponBeforeGiveCacheService.refreshCouponBeforeGiveCache(in.getCouponBeforeGiveId());
        return BackendResponseUtil.success();
    }


    @RequestMapping(value = "/couponRead/queryCouponTypePG.do", consumes = "application/json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "查询优惠券明细列表")
    public ResponseDto<PageResponse> queryCouponTypePg(@RequestBody PageRequest<CouponQueryRequest> requestBody) throws Exception {
        CouponQueryBo bo = requestBody.getObj().convert();
        bo.setCurrentPage(requestBody.getCurrentPage());
        bo.setItemsPerPage(requestBody.getItemsPerPage());
        PageResponse<CouponViewResponse> pageResponse = couponService.queryCouponByPageRequest(bo);
        return BackendResponseUtil.successObj(pageResponse);
    }

    @RequestMapping(value = "/couponRead/countCouponTypePG.do", consumes = "application/json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "查询券总数")
    public ResponseDto<PageResponse<CouponViewResponse>> countCouponTypePG(@RequestBody PageRequest<CouponQueryRequest> requestBody) throws Exception {
        CouponQueryBo bo = requestBody.getObj().convert();
        bo.setCurrentPage(0);
        bo.setItemsPerPage(1);
        PageResponse<CouponViewResponse> pageResponse = couponService.queryCouponByPageRequest(bo);
        return BackendResponseUtil.successObj(pageResponse);
    }

    @PostMapping(value = "/couponRead/exportCoupons.do")
    @ResponseBody
    @ApiOperation(value = "导出优惠券明细列表")
    public ResponseDto<Void> exportCoupons(@RequestBody @Validated CouponExportRequest request) {
        CouponExportBo bo = request.convert();
        couponService.exportCouponListAsync(bo);
        return BackendResponseUtil.success();
    }

    @PostMapping(value = "/couponRead/exportDonateCoupons.do")
    @ResponseBody
    @ApiOperation(value = "导出赠送优惠券明细列表")
    public ResponseDto<Void> exportDonateCoupons(@RequestBody @Validated DonateCouponsExportRequest request) {
        DonateCouponsExportBo bo = request.convert();
        couponService.exportDonateCouponListAsync(bo);
        return BackendResponseUtil.success();
    }
}
