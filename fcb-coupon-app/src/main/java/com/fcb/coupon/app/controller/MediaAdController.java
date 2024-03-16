package com.fcb.coupon.app.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fcb.coupon.app.business.couponreceive.CouponReceiveBusiness;
import com.fcb.coupon.app.exception.CouponReceiveErrorCode;
import com.fcb.coupon.app.exception.MediaCouponErrorCode;
import com.fcb.coupon.app.model.bo.CouponReceiveBo;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.param.request.JudgeCouponCanUse4MediaRequest;
import com.fcb.coupon.app.model.param.request.ReceiveCoupon4MediaRequest;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.CouponGiveRuleEnum;
import com.fcb.coupon.common.enums.CouponThemeStatus;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.CommonResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.MessageFormat;
import java.util.Date;

/**
 * @author YangHanBin
 * @date 2021-08-16 18:10
 */
@RestController
@Api(tags = {"媒体广告相关"})
@RequestMapping
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class MediaAdController {
    private final CouponReceiveBusiness couponReceiveBusiness;

    @ApiOperation(value = "媒体广告--领券", httpMethod = "POST")
    @PostMapping(value = "/inner-api/promotion/coupon/receiveCoupon4Media.do")
    public ResponseDto<Void> receiveCoupon4Media(@RequestBody @Valid ReceiveCoupon4MediaRequest in) {
        CouponReceiveBo bo = in.convert();

        CouponThemeCache couponThemeCache = couponReceiveBusiness.getCouponThemeCache(bo.getCouponThemeId());
        if (!CouponGiveRuleEnum.COUPON_GIVE_RULE_MEDIA_ADVERT.ifSame(couponThemeCache.getCouponGiveRule())) {
            log.error("媒体广告--领券 error: couponGiveRule={}", couponThemeCache.getCouponGiveRule());
            CouponReceiveErrorCode errorCode = CouponReceiveErrorCode.COUPON_GIVE_RULE_ILLEGAL;
            String realMessage = String.format(errorCode.getMessage(), CouponGiveRuleEnum.COUPON_GIVE_RULE_MEDIA_ADVERT.getTypeStr());
            errorCode.setMessage(realMessage);
            throw new BusinessException(errorCode);
        }

        couponReceiveBusiness.receive(bo, couponThemeCache);
        return CommonResponseUtil.success();
    }

    @ApiOperation(value = "媒体广告-判断couponThemeId是否可用")
    @PostMapping(value = "/inner-api/promotion/coupon/judgeCouponCanUse4Media.do")
    public ResponseDto<Void> judgeCouponCanUse4Media(@RequestBody JudgeCouponCanUse4MediaRequest in) {
        CouponThemeCache couponThemeCache = couponReceiveBusiness.getCouponThemeCache(in.getCouponThemeId());
        Date now = new Date();
        // 活动已结束
        if (now.after(couponThemeCache.getEndTime())) {
            throw new BusinessException(MediaCouponErrorCode.COUPON_END);
        }

        // 状态不是进行中
        Integer status = couponThemeCache.getStatus();
        if (!CouponThemeStatus.EFFECTIVE.getStatus().equals(status) && !CouponThemeStatus.APPROVED.getStatus().equals(status)) {
            throw new BusinessException(MediaCouponErrorCode.COUPON_THEME_STATUS_NOT_ALLOW.getCode(), MessageFormat.format(MediaCouponErrorCode.COUPON_THEME_STATUS_NOT_ALLOW.getMessage(), CouponThemeStatus.of(status).getDesc()));
        }

        //非媒体广告券不能领券
        if (!CouponGiveRuleEnum.COUPON_GIVE_RULE_MEDIA_ADVERT.ifSame(couponThemeCache.getCouponGiveRule())) {
            throw new BusinessException(MediaCouponErrorCode.NOT_MEDIA_ADVERT_COUPON);
        }

        JSONArray ids = new JSONArray();
        try {
            String applicableUserTypes = couponThemeCache.getApplicableUserTypes();
            ids = JSONObject.parseObject(applicableUserTypes).getJSONArray("ids");
        } catch (Exception e) {
            log.error("根据themeId查询券是否可用,使用人群解析异常couponThemeId={},e={}", couponThemeCache.getId(), e.getMessage());
        }
        // 校验是否C端用户可用的券
        if (!ids.contains(UserTypeEnum.C.getUserType())) {
            throw new BusinessException(MediaCouponErrorCode.USER_TYPE_NOT_ALLOW);
        }

        return CommonResponseUtil.success();
    }
}
