package com.fcb.coupon.app.controller;

import com.fcb.coupon.app.business.couponreceive.CouponReceiveBusiness;
import com.fcb.coupon.app.exception.CouponReceiveErrorCode;
import com.fcb.coupon.app.exception.CouponThemeErrorCode;
import com.fcb.coupon.app.infra.inteceptor.AppAuthorityHolder;
import com.fcb.coupon.app.infra.inteceptor.AppLogin;
import com.fcb.coupon.app.infra.inteceptor.AppUserInfo;
import com.fcb.coupon.app.model.bo.CouponReceiveBo;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponVideoEntity;
import com.fcb.coupon.app.model.param.request.ReceiveVideoLiveCouponRequest;
import com.fcb.coupon.app.model.param.request.ThemeIdRequest;
import com.fcb.coupon.app.model.param.response.CouponThemeCountOutputResponse;
import com.fcb.coupon.app.properties.CouponReceiveProperties;
import com.fcb.coupon.app.service.CouponThemeCacheService;
import com.fcb.coupon.app.service.CouponVideoService;
import com.fcb.coupon.common.constant.RedisLockKeyConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.CouponGiveRuleEnum;
import com.fcb.coupon.common.enums.CouponSourceTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.CommonResponseUtil;
import com.fcb.coupon.common.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.Objects;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月25日 19:09:00
 */
@RestController
@Api(tags = "视频直播相关接口")
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class VideoLiveController {

    private final CouponReceiveBusiness couponReceiveBusiness;
    private final RedissonClient redissonClient;
    private final CouponVideoService couponVideoService;
    private final CouponReceiveProperties couponReceiveProperties;
    private final CouponThemeCacheService couponThemeCacheService;

    // 视频领券限流器
    private RRateLimiter videoRateLimiter;

    @PostConstruct
    public void init() {
        videoRateLimiter = redissonClient.getRateLimiter("VIDEO_LIVE_RECEIVE_COUPON_RATE_LIMITER");
        videoRateLimiter.trySetRate(RateType.OVERALL, couponReceiveProperties.getVideoLimitRate(), couponReceiveProperties.getVideoLimitInterval(), RateIntervalUnit.SECONDS);
        log.info("限流器初始化完成!!!");
    }

    @ApiOperation(value = "微信视频号查询优惠券活动券库存数量--C端直接调用")
    @PostMapping(value = "/api/promotion/couponTheme/getCount")
    public ResponseDto<CouponThemeCountOutputResponse> getCouponThemeValidCount(@RequestBody @Validated ThemeIdRequest request) {
        if (Objects.isNull(request.getCouponThemeId())) {
            return CommonResponseUtil.fail(CouponThemeErrorCode.COUPON_THEME_ID_CANNOT_NULL);
        }
        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(request.getCouponThemeId());

        CouponThemeCountOutputResponse response = new CouponThemeCountOutputResponse();
        if (couponThemeCache != null) {
            response.setCouponThemeId(request.getCouponThemeId());
            response.setStockCount(couponThemeCache.getCreatedCount() - couponThemeCache.getSendedCount());
        }

        return CommonResponseUtil.successObj(response);
    }

    @ApiOperation(value = "微信视频号直播领券--C端直接调用", httpMethod = "POST")
    @PostMapping(value = "/api/promotion/coupon/receiveVideoLiveCoupon", produces = {"application/json;charset=UTF-8"})
    @AppLogin
    public ResponseDto<Void> receiveCoupon4VideoLive(@RequestBody @Valid ReceiveVideoLiveCouponRequest request) {
        // 限流
        if (!videoRateLimiter.tryAcquire(1)) {
            throw new BusinessException(CouponReceiveErrorCode.OUT_OF_RATE_LIMIT);
        }

        CouponReceiveBo bo = new CouponReceiveBo();
        bo.setSource(CouponSourceTypeEnum.COUPON_SOURCE_VIDEO_LIVE.getSource());
        bo.setSourceId(request.getVideoNo());
        bo.setReceiveCount(1);

        AppUserInfo userInfo = AppAuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        bo.setUserId(userInfo.getUserId());
        bo.setUserMobile(userInfo.getUserMobile());
        bo.setUserType(userInfo.getUserType());

        //限制5s的访问频率
        String lockKey = RedisLockKeyConstant.getCouponReceiveLockKey(bo.getCouponThemeId(), bo.getUserId());
        if (!RedisUtil.tryLock(lockKey, "", 5)) {
            throw new BusinessException(CouponReceiveErrorCode.SYSTEM_BUSY_LIMIT);
        }

        //领券优惠券
        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(bo.getCouponThemeId());
        //视频直播只能领券营销活动页券
        if (!CouponGiveRuleEnum.COUPON_GIVE_RULE_MARKETING_ACTIVITY.getType().equals(couponThemeCache.getCouponGiveRule())) {
            throw new BusinessException(CouponReceiveErrorCode.SOURCE_ILLEGAL);
        }

        CouponEntity couponEntity = couponReceiveBusiness.receive(bo, couponThemeCache);

        //保存视频直播领券信息
        CouponVideoEntity couponVideoEntity = new CouponVideoEntity();
        couponVideoEntity.setCouponId(couponEntity.getId());
        couponVideoEntity.setCity(request.getCity());
        couponVideoEntity.setName(request.getCustomerName());
        couponVideoEntity.setOpenId(request.getOpenid());
        couponVideoEntity.setCouponThemeId(request.getCouponThemeId());
        couponVideoEntity.setVideoNo(request.getVideoNo());

        try {
            couponVideoService.save(couponVideoEntity);
        } catch (Exception ex) {
            log.error("视频直播领券信息保存异常，参数:{}", couponVideoEntity, ex);
        }

        return CommonResponseUtil.success("恭喜成功领券", null);
    }
}
