package com.fcb.coupon.backend.controller;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.backend.business.couponSend.CouponSendBusiness;
import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.infra.inteceptor.IgnoreLogin;
import com.fcb.coupon.backend.model.bo.CouponBatchSendBo;
import com.fcb.coupon.backend.model.bo.CouponSendUserBo;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.dto.CouponSendResult;
import com.fcb.coupon.backend.model.dto.CouponThemeCrowdScopeIdDto;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.service.CouponThemeService;
import com.fcb.coupon.client.backend.param.request.ActivitySendCouponRequest;
import com.fcb.coupon.client.backend.param.request.CouponThemeIdsCrmRequest;
import com.fcb.coupon.client.backend.param.request.CouponThemeListCmsRequest;
import com.fcb.coupon.client.backend.param.response.ActivitySendCouponResponse;
import com.fcb.coupon.client.backend.param.response.CouponThemeCrmResponse;
import com.fcb.coupon.client.backend.param.response.CouponThemeListCmsResponse;
import com.fcb.coupon.client.dto.RestResult;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 唐陆军
 * @Description 注：优惠券内部依赖接口,不能对外暴露
 * @createTime 2021年08月24日 09:01:00
 */
@RestController
@Api(tags = "优惠券接口")
public class SoaCloudApiController {

    @Autowired
    private CouponSendBusiness couponSendBusiness;

    @Autowired
    private CouponThemeService couponThemeService;

    @IgnoreLogin
    @PostMapping(value = "/cloud/couponBackWrite/sendCoupons4activity")
    @ResponseBody
    @ApiOperation(value = "活动发券")
    public RestResult<ActivitySendCouponResponse> sendActivityCoupon(@RequestBody @Valid ActivitySendCouponRequest request) {
        //活动发券限制是单条发送，保持参数兼容发送用户对象设计为数组
        if (request.getData().getSendUsers().size() != 1) {
            return new RestResult(CommonErrorCode.PARAMS_ERROR.getCode(), "只允许发送一个用户", null);
        }
        ActivitySendCouponRequest.ActivitySendCouponDataRequest data = request.getData();
        if (data == null) {
            throw new BusinessException(CommonErrorCode.PARAMS_ERROR);
        }
        //参数构造
        CouponBatchSendBo bo = new CouponBatchSendBo();
        bo.setThemeId(data.getCouponThemeId());
        bo.setSource(data.getSource());
        bo.setSourceId(data.getSourceId());
        bo.setSendUserType(data.getSendCouponUserType());
        if (UserTypeEnum.C.getUserType().equals(bo.getSendUserType()) || UserTypeEnum.B.getUserType().equals(bo.getSendUserType())) {
            List<CouponSendUserBo> couponSendUserBos = new ArrayList<>();
            for (ActivitySendCouponRequest.ActivitySendCouponUserRequest sendUser : data.getSendUsers()) {
                CouponSendUserBo couponSendUserBo = new CouponSendUserBo();
                couponSendUserBo.setUserId(sendUser.getUserId());
                couponSendUserBo.setUnionId(sendUser.getUnionId());
                couponSendUserBo.setTransactionId(data.getBatchNo());
                couponSendUserBo.setCount(1);
                couponSendUserBos.add(couponSendUserBo);
            }
            bo.setSendUserList(couponSendUserBos);
        } else if (UserTypeEnum.SAAS.getUserType().equals(bo.getSendUserType())) {
            List<CouponSendUserBo> couponSendUserBos = new ArrayList<>();
            for (ActivitySendCouponRequest.ActivitySendCouponUserRequest sendUser : data.getSendUsers()) {
                CouponSendUserBo couponSendUserBo = new CouponSendUserBo();
                couponSendUserBo.setUserId(sendUser.getSaasUserId());
                couponSendUserBo.setTransactionId(data.getBatchNo());
                couponSendUserBo.setCount(1);
                couponSendUserBos.add(couponSendUserBo);
            }
            bo.setSendUserList(couponSendUserBos);
        }

        CouponSendResult result = couponSendBusiness.activityBatchSend(bo);
        CouponSendContext sendContext = result.getSendContexts().get(0);

        //构造返回结果
        ActivitySendCouponResponse activitySendCouponResponse = new ActivitySendCouponResponse();
        ActivitySendCouponResponse.ActivitySendCouponDataResponse activitySendCouponDataResponse = new ActivitySendCouponResponse.ActivitySendCouponDataResponse();
        BeanUtils.copyProperties(sendContext, activitySendCouponDataResponse);
        activitySendCouponResponse.setData(Lists.newArrayList(activitySendCouponDataResponse));
        if (!sendContext.getIsFailure()) {
            activitySendCouponDataResponse.setCouponId(sendContext.getCouponEntity().getId());
            return new RestResult("发券成功", activitySendCouponResponse);
        } else {
            return new RestResult(CouponThemeErrorCode.COUPON_SEND_ERROR.getCode(), "发券失败：" + sendContext.getFailureReason(), activitySendCouponResponse);
        }
    }


    /**
     * 根据id获取券活动信息(CRM调用)
     */
    @IgnoreLogin
    @PostMapping(value = "/cloud/couponThemeBackRead/getCouponThemeDetailsByThemeIds")
    @ResponseBody
    @ApiOperation(value = "根据id获取券活动信息", httpMethod = "POST")
    public RestResult<List<CouponThemeCrmResponse>> getCouponThemeDetailsByThemeIds(@RequestBody @Validated CouponThemeIdsCrmRequest request) {
        List<CouponThemeEntity> couponThemeEntities = couponThemeService.listByIds(request.getData().getThemeIds());
        List<CouponThemeCrmResponse> couponThemeCrmResponses = new ArrayList<>();
        for (CouponThemeEntity couponThemeEntity : couponThemeEntities) {
            CouponThemeCrmResponse couponThemeCrmResponse = new CouponThemeCrmResponse();
            couponThemeCrmResponse.setId(couponThemeEntity.getId());
            couponThemeCrmResponse.setThemeTitle(couponThemeEntity.getThemeTitle());
            couponThemeCrmResponse.setCanAssign(couponThemeEntity.getCanTransfer());
            couponThemeCrmResponse.setCanDonation(couponThemeEntity.getCanDonation());
            if (StringUtils.isNotBlank(couponThemeEntity.getApplicableUserTypes())) {
                CouponThemeCrowdScopeIdDto dto = JSON.parseObject(couponThemeEntity.getApplicableUserTypes(), CouponThemeCrowdScopeIdDto.class);
                couponThemeCrmResponse.setCrowdIds(dto.getIds());
            }
            couponThemeCrmResponses.add(couponThemeCrmResponse);
        }
        return new RestResult(couponThemeCrmResponses);
    }


    /**
     * 根据id获取券活动信息-配置营销活动页调用(CMS服务调用)
     */
    @IgnoreLogin
    @PostMapping(value = "/cloud/couponBackRead/getCouponThemeList")
    @ResponseBody
    @ApiOperation(value = "根据id获取券活动信息", httpMethod = "POST")
    public RestResult<CouponThemeListCmsResponse> getCouponThemeList(@RequestBody @Validated CouponThemeListCmsRequest request) {
        CouponThemeListCmsResponse response = couponThemeService.listByCms(request);
        return new RestResult(response);
    }
}
