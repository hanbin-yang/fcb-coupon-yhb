package com.fcb.coupon.app.controller;

import com.fcb.coupon.app.business.couponreceive.CouponReceiveBusiness;
import com.fcb.coupon.app.exception.CouponReceiveErrorCode;
import com.fcb.coupon.app.infra.inteceptor.AppLogin;
import com.fcb.coupon.app.model.bo.CouponReceiveBo;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.param.request.ReceiveCoupon4BuildingRequest;
import com.fcb.coupon.app.model.param.request.ReceiveCoupon4CommonRequest;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.CouponGiveRuleEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.BaseRSAUtils;
import com.fcb.coupon.common.util.CommonResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author YangHanBin
 * @date 2021-08-17 9:29
 */
@RestController
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@RequestMapping
@Api(tags = {"B端、C端共用的接口"})
@RefreshScope
@Slf4j
public class CustomerAndMemberController implements InitializingBean {
    private final CouponReceiveBusiness couponReceiveBusiness;
    private final RedissonClient redissonClient;

    @Value("${promotion.common.RSAPrivateKey}")
    private String rSAPrivateKey;

    @Value("${receive.coupon.limit.rate:60}")
    private Long rate;
    // 用来刷新限流器
    private Long internalRate;
    // 通用领券限流器
    private RRateLimiter commonReceiveRateLimiter;

    @Override
    public void afterPropertiesSet() throws Exception {
        commonReceiveRateLimiter = redissonClient.getRateLimiter("COMMON_RECEIVE_COUPON_RATE_LIMITER");
        refreshRateLimit();
        log.info("CustomerAndMemberController#afterPropertiesSet finish!!!");
    }

    @ApiOperation(value = "B/C端 楼盘--领券", httpMethod = "POST")
    @PostMapping(value = "/inner-api/promotion/coupon/receiveCoupon4broker.do")
    @AppLogin
    public ResponseDto<Void> receiveCoupon4Building(@RequestBody @Valid ReceiveCoupon4BuildingRequest in) throws Exception {
        CouponReceiveBo bo = in.convert();

        // couponThemeId解密
        long couponThemeId = Long.parseLong(BaseRSAUtils.decryptByPrivateKey(in.getCouponThemeId(), rSAPrivateKey));
        bo.setCouponThemeId(couponThemeId);
        CouponThemeCache couponThemeCache = couponReceiveBusiness.getCouponThemeCache(couponThemeId);
        if (!CouponGiveRuleEnum.COUPON_GIVE_RULE_FRONT.ifSame(couponThemeCache.getCouponGiveRule())) {
            log.error("楼盘--领券 error: couponGiveRule={}", couponThemeCache.getCouponGiveRule());
            CouponReceiveErrorCode errorCode = CouponReceiveErrorCode.COUPON_GIVE_RULE_ILLEGAL;
            String realMessage = String.format(errorCode.getMessage(), CouponGiveRuleEnum.COUPON_GIVE_RULE_FRONT.getTypeStr());
            errorCode.setMessage(realMessage);
            throw new BusinessException(errorCode);
        }
        couponReceiveBusiness.receive(bo, couponThemeCache);
        return CommonResponseUtil.success();

    }

    @ApiOperation(value = "B/C端 通用领券", httpMethod = "POST")
    @PostMapping(value = "/api/promotion/coupon/receiveCoupon.do", produces = {"application/json;charset=UTF-8"})
    @AppLogin
    public ResponseDto<Void> receiveCoupon4Common(@RequestBody @Valid ReceiveCoupon4CommonRequest in) {
        // 限流
        if (!internalRate.equals(rate)) {
            refreshRateLimit();
        }
        if (!commonReceiveRateLimiter.tryAcquire(1)) {
            return CommonResponseUtil.fail(CouponReceiveErrorCode.OUT_OF_RATE_LIMIT);
        }
        CouponReceiveBo bo = in.convert();
        CouponThemeCache couponThemeCache = couponReceiveBusiness.getCouponThemeCache(bo.getCouponThemeId());
        couponReceiveBusiness.receive(bo, couponThemeCache);
        return CommonResponseUtil.success();
    }

    private void refreshRateLimit() {
        this.commonReceiveRateLimiter.delete();
        boolean trySetRate = commonReceiveRateLimiter.trySetRate(RateType.OVERALL, rate, 1, RateIntervalUnit.SECONDS);
        this.internalRate = this.rate;
        log.info("领取优惠券限流refresh: rateLimiterName={}, trySetRate={}", commonReceiveRateLimiter.getName(), trySetRate);
    }

}
