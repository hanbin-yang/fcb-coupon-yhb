package com.fcb.coupon.app.controller;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.app.business.mingyuan.MingYuanVerificationBusiness;
import com.fcb.coupon.app.business.mingyuan.MingYuanVerificationReadBusiness;
import com.fcb.coupon.app.exception.Coupon4OrderErrorCode;
import com.fcb.coupon.app.model.bo.CouponMingyuanBo;
import com.fcb.coupon.app.model.bo.OperateCouponBo;
import com.fcb.coupon.app.model.dto.OperateCoupons4OrderInputDto;
import com.fcb.coupon.app.model.param.request.CouponMingyuanRequest;
import com.fcb.coupon.app.model.bo.CheckCouponUsefulBo;
import com.fcb.coupon.app.model.bo.QueryUsefulCouponBo;
import com.fcb.coupon.app.model.param.request.CheckCouponUsefulRequest;
import com.fcb.coupon.app.model.param.request.OperateCoupons4OrderRequest;
import com.fcb.coupon.app.model.param.request.QueryUsefulCouponRequest;
import com.fcb.coupon.app.model.param.response.CouponMingyuanResponse;
import com.fcb.coupon.app.model.param.response.CheckCouponUsefulResponse;
import com.fcb.coupon.app.model.param.response.OperateCoupons4OrderResponse;
import com.fcb.coupon.app.model.param.response.QueryUsefulCouponResponse;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.RedisLockKeyConstant;
import com.fcb.coupon.common.constant.VerificationChannel;
import com.fcb.coupon.common.dto.RedisLockResult;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.util.CommonResponseUtil;
import com.fcb.coupon.common.util.MobileValidateUtil;
import com.fcb.coupon.common.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;

/**
 * @author YangHanBin
 * @date 2021-08-24 9:52
 */
@Api(tags = {"明源核销相关"})
@Controller
@RequestMapping
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class MingYuanController {
    private final MingYuanVerificationBusiness mingYuanVerificationBusiness;
    private final MingYuanVerificationReadBusiness mingYuanVerificationReadBusiness;
    @ResponseBody
    @PostMapping(value = "/inner-api/promotion/coupon/operateCoupons4Order.do", consumes = "application/json")
    @ApiOperation(value = "明源-操作优惠券（上锁/解锁/换绑交易id/核销）")
    public ResponseDto<List<OperateCoupons4OrderResponse>> operateCoupons4Order(@RequestBody OperateCoupons4OrderRequest in) {
        long startTime = System.currentTimeMillis();
        List<OperateCoupons4OrderInputDto> list = null;
        try {
            list = JSON.parseArray(in.getList(), OperateCoupons4OrderInputDto.class);
        } catch (Exception e) {
            log.error("operateCoupons4Order 解析JSON异常", e);
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.SERVICE_EXCEPTION);
        }

        if (CollectionUtils.isEmpty(list)){
            log.error("请求业务参数为空 ：{}",list);
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.REQUEST_DATA_EMPTY_ERROR);
        }
        OperateCoupons4OrderInputDto dto = list.get(0);

        OperateCouponBo bo = new OperateCouponBo();
        BeanUtils.copyProperties(dto, bo);
        if (Objects.isNull(bo.getUsedChannel())) {
            bo.setUsedChannel(VerificationChannel.MINGYUAN);
        }

        String keyName = RedisLockKeyConstant.COUPON_OPERATION.concat(bo.getTransactionId());
        RedisLockResult<List<OperateCoupons4OrderResponse>> redisLockResult = RedisUtil.executeTryLock(keyName, CouponConstant.NO, () -> mingYuanVerificationBusiness.operateCoupons4Order(bo));
        if (redisLockResult.isFailure()) {
            log.error("明源操作优惠券获取分布式锁失败 keyName={}", keyName);
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.FREQUENT_OPERATE);
        }

        List<OperateCoupons4OrderResponse> vo = redisLockResult.getObj();

        log.info("明源操作优惠券end: in={}, output={}, 耗时：{}ms", JSON.toJSONString(in), JSON.toJSONString(vo), System.currentTimeMillis() - startTime);
        if (CollectionUtils.isNotEmpty(vo)) {
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.FAIL, vo);
        }

        return CommonResponseUtil.successObj(vo);
    }

    @ResponseBody
    @PostMapping(value = "/inner-api/promotion/coupon/getCanUseCoupons4Order.do", consumes = "application/json")
    @ApiOperation(value = "明源-查询可用券列表")
    public ResponseDto<List<QueryUsefulCouponResponse>> getCanUseCoupons4Order(@RequestBody QueryUsefulCouponRequest in) {
        List<QueryUsefulCouponBo> list = JSON.parseArray(in.getList(), QueryUsefulCouponBo.class);
        if (CollectionUtils.isEmpty(list)) {
            log.error("请求业务参数为空：{}", list);
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.REQUEST_DATA_EMPTY_ERROR);
        }
        QueryUsefulCouponBo bo = list.get(0);

        //项目id
        if (StringUtil.isBlank(bo.getItemId())) {
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.ITEM_ID_REQUIRED, null);
        }

        //手机号不能为空
        if (StringUtil.isBlank(bo.getPhone())) {
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.PHONE_NOT_NULL, null);
        }

        //手机号格式
        if (!MobileValidateUtil.isMobile(bo.getPhone())) {
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.PHONE_FORMAT_IS_WRONG, null);
        }

        //房源id
        if (StringUtil.isBlank(bo.getRoomGuid())) {
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.ROOM_GUID_REQUIRED, null);
        }

        //物业类型
        if (Objects.isNull(bo.getPropertyType())) {
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.PROPERTY_TYPE_REQUIRED, null);
        }

        List<QueryUsefulCouponResponse> result = mingYuanVerificationReadBusiness.queryCouponList4Order(bo);
        if (CollectionUtils.isEmpty(result)) {
            return CommonResponseUtil.successObj(null);
        }
        return CommonResponseUtil.successObj(result);
    }

    /**
     * 校验优惠券是否可用
     */
    @ApiOperation(value = "明源-校验券是否可用")
    @ResponseBody
    @PostMapping(value = "/inner-api/promotion/coupon/checkCoupons4Order", consumes = "application/json")
    public ResponseDto<List<CheckCouponUsefulResponse>> checkCoupons4Order(@RequestBody CheckCouponUsefulRequest in) {
        List<CheckCouponUsefulBo> list = JSON.parseArray(in.getList(), CheckCouponUsefulBo.class);

        if (CollectionUtils.isEmpty(list)){
            log.error("请求业务参数为空：{}",list);
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.REQUEST_DATA_EMPTY_ERROR);
        }
        CheckCouponUsefulBo bo = list.get(0);

        if(CollectionUtils.isEmpty(bo.getCheckCoupons())){
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.COUPON_IDS_REQUIRED);
        }

        //项目id
        if (StringUtil.isBlank(bo.getItemId())) {
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.ITEM_ID_REQUIRED);
        }

        //手机号不能为空
        if (StringUtil.isBlank(bo.getPhone())) {
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.PHONE_NOT_NULL, null);
        }

        //手机号
        if (!MobileValidateUtil.isMobile(bo.getPhone())) {
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.PHONE_FORMAT_IS_WRONG);
        }

        //房源id
        if (StringUtil.isBlank(bo.getRoomGuid())) {
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.ROOM_GUID_REQUIRED);
        }

        //物业类型
        if (Objects.isNull(bo.getPropertyType())) {
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.PROPERTY_TYPE_REQUIRED);
        }

        ResponseDto<List<CheckCouponUsefulResponse>> out = mingYuanVerificationReadBusiness.validateCoupons4OrderForQuery(bo);
        return out;

    }

    @ApiOperation(value = "明源-根据券id等查询对应优惠券信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "couponRequest", value = "请求参数", dataType = "CouponMingyuanRequest")})
    @ResponseBody
    @PostMapping(value = "/inner-api/promotion/coupon/queryCouponListByIds.do")
    public ResponseDto<List<CouponMingyuanResponse>> getMingyuanCouponListByIds(@RequestBody CouponMingyuanRequest couponRequest) {
        CouponMingyuanBo bo = couponRequest.convert();
        return CommonResponseUtil.successObj(mingYuanVerificationReadBusiness.getMingyuanCouponListByIds(bo));
    }

}
