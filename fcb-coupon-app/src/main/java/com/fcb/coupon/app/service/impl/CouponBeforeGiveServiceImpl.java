package com.fcb.coupon.app.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.app.exception.CouponBeforeGiveErrorCode;
import com.fcb.coupon.app.infra.inteceptor.AppAuthorityHolder;
import com.fcb.coupon.app.infra.inteceptor.AppUserInfo;
import com.fcb.coupon.app.mapper.CouponBeforeGiveMapper;
import com.fcb.coupon.app.model.bo.CaptchasBo;
import com.fcb.coupon.app.model.bo.CouponBeforeGiveAddBo;
import com.fcb.coupon.app.model.bo.CouponBeforeGiveAddMessageBo;
import com.fcb.coupon.app.model.bo.ReceiveBeforeGivingBo;
import com.fcb.coupon.app.model.dto.CouponBeforeGiveCacheDto;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.dto.ProfileDto;
import com.fcb.coupon.app.model.entity.CouponBeforeGiveEntity;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponUserEntity;
import com.fcb.coupon.app.model.param.response.CouponBeforeGiveAddResponse;
import com.fcb.coupon.app.model.param.response.ReceiveBeforeGivingResponse;
import com.fcb.coupon.app.remote.user.CustomerFeignClient;
import com.fcb.coupon.app.remote.user.MemberFeignClient;
import com.fcb.coupon.app.remote.user.ProfileFeignClient;
import com.fcb.coupon.app.remote.user.SaasFeignClient;
import com.fcb.coupon.app.service.CaptchasService;
import com.fcb.coupon.app.service.CouponBeforeGiveCacheService;
import com.fcb.coupon.app.service.CouponBeforeGiveService;
import com.fcb.coupon.app.service.CouponService;
import com.fcb.coupon.app.service.CouponThemeCacheService;
import com.fcb.coupon.app.service.CouponUserService;
import com.fcb.coupon.app.uitls.NumberConvertUtils;
import com.fcb.coupon.common.constant.RestConstant;
import com.fcb.coupon.common.enums.BusinessTypeEnum;
import com.fcb.coupon.common.enums.ClientTypeEnum;
import com.fcb.coupon.common.enums.CouponDiscountType;
import com.fcb.coupon.common.enums.CouponGiveTypeEnum;
import com.fcb.coupon.common.enums.CouponSourceTypeEnum;
import com.fcb.coupon.common.enums.CouponStatusEnum;
import com.fcb.coupon.common.enums.YesNoEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.util.AESPromotionUtil;
import com.fcb.coupon.common.util.DesensitizeUtil;
import com.fcb.coupon.common.util.MobileValidateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author YangHanBin
 * @date 2021-08-13 9:57
 */
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class CouponBeforeGiveServiceImpl extends ServiceImpl<CouponBeforeGiveMapper, CouponBeforeGiveEntity> implements CouponBeforeGiveService {
    private final CouponBeforeGiveCacheService couponBeforeGiveCacheService;
    private final CouponService couponService;
    private final CouponUserService couponUserService;
    private final CouponThemeCacheService couponThemeCacheService;
    private final ProfileFeignClient profileFeignClient;
    private final MemberFeignClient memberFeignClient;
    private final CaptchasService captchasService;
    private final SaasFeignClient saasFeignClient;
    private final CustomerFeignClient customerFeignClient;
    private final KafkaTemplate kafkaTemplate;


    private final static String COUPON_TRANSFER_CODE = "coupon_transfer";

    @Value(("${remote.url.m.short}"))
    private String urlShortBase;

    @Value(("${coupon.givetype.sms.limit}"))
    private Integer giveTypeSmsLimit;


    /**
     * 检查券转赠中数
     *
     * @param couponId
     * @return
     */
    @Override
    public Integer getBeforeGiveCount(Long couponId) {
        LambdaQueryWrapper<CouponBeforeGiveEntity> queryWrapper = Wrappers.lambdaQuery(CouponBeforeGiveEntity.class);
        queryWrapper.eq(CouponBeforeGiveEntity::getCouponId, couponId);
        queryWrapper.gt(CouponBeforeGiveEntity::getExpireTime, new Date());
        int beforeGiveCount = this.baseMapper.selectCount(queryWrapper);
        return beforeGiveCount;
    }

    @Override
    public ReceiveBeforeGivingResponse getCouponBeforeGivingInfo(ReceiveBeforeGivingBo bo) {
        // 缓存中获取待转增信息
        CouponBeforeGiveCacheDto couponBeforeGiveCache = couponBeforeGiveCacheService.getById(bo.getCouponBeforeGiveId());
        if (Objects.isNull(couponBeforeGiveCache)) {
            log.info("没有券信息");
            return null;
        }

        String content = "立即领取";
        ReceiveBeforeGivingResponse resp = new ReceiveBeforeGivingResponse();
        BeanUtils.copyProperties(couponBeforeGiveCache, resp);
        String usernameOrMobile = "***";
        if (StringUtils.isNotBlank(couponBeforeGiveCache.getGiveNickname())) {
            // 用户名脱敏
            usernameOrMobile = DesensitizeUtil.around(couponBeforeGiveCache.getGiveNickname(), 1, 1);
        } else {
            // 手机号脱敏
            usernameOrMobile = DesensitizeUtil.around(couponBeforeGiveCache.getGiveUserMobile(), 3, 4);
        }
        resp.setGiveUsernameOrMobile(usernameOrMobile);

        Date nowDate = new Date();
        //券过期时间
        Date expireTime = couponBeforeGiveCache.getExpireTime();
        // 用户登录了进来的
        Long receiveUserId = bo.getReceiveUserId();

        if (Objects.nonNull(receiveUserId)) {
            if (Objects.equals(couponBeforeGiveCache.getReceiveUserId(), receiveUserId)) {
                log.info("您已经领取过了。领取人id【{}】，券指定领取人id【{}】", receiveUserId, couponBeforeGiveCache.getReceiveUserId());
                content = "您已经领取过了";
            } else if (Objects.nonNull(couponBeforeGiveCache.getReceiveUserId())) {

                log.info("来晚了，福利已经派完了。券指定领取人id【{}】", couponBeforeGiveCache.getReceiveUserId());

                content = "来晚了，福利已经派完了";
            } else if (expireTime.before(nowDate)) {

                log.info("来晚了，领取已失效。券有效时间【{}】，当前时间【{}】", expireTime, nowDate);

                content = "来晚了，领取已失效";
            } else if (!CouponStatusEnum.STATUS_ISSUE.getStatus().equals(couponBeforeGiveCache.getCouponStatus()) && !CouponStatusEnum.STATUS_USE.getStatus().equals(couponBeforeGiveCache.getCouponStatus())) {

                log.info("当前优惠券状态发生变化，无法领取。券状态【{}】", couponBeforeGiveCache.getCouponStatus());

                content = "当前优惠券状态发生变化，无法领取";
            }
        } else if (expireTime.before(nowDate)) {

            log.info("来晚了，领取已失效。券有效时间【{}】，当前时间【{}】", expireTime, nowDate);

            content = "来晚了，领取已失效";
        }

        resp.setContent(content);
        return resp;
    }

    /**
     * 添加转赠前的优惠券信息
     */
    @Override
    public CouponBeforeGiveAddResponse addCouponBeforeGiveById(CouponBeforeGiveAddBo bo) {
        CouponBeforeGiveEntity entity = new CouponBeforeGiveEntity();
        //校验参数合法性
        validateParam(bo);
        //初始化数据
        CouponEntity couponEntity = initDtoCommon(bo, entity);
        //非短信不允许重复添加
        if (!Objects.equals(CouponGiveTypeEnum.TYPE_SMS.getType(), entity.getGiveType())) {
            LambdaQueryWrapper<CouponBeforeGiveEntity> queryWrapper = Wrappers.lambdaQuery(CouponBeforeGiveEntity.class);
            queryWrapper.eq(CouponBeforeGiveEntity::getCouponId, entity.getCouponId()).
            eq(CouponBeforeGiveEntity::getGiveType, entity.getGiveType()).eq(CouponBeforeGiveEntity::getTerminalType, entity.getTerminalType());

            if (!Objects.isNull(entity.getGiveUserid())) {
                queryWrapper.eq(CouponBeforeGiveEntity::getGiveUserid, entity.getGiveUserid());
            }
            List<CouponBeforeGiveEntity> list = this.list(queryWrapper);
            if (!CollectionUtils.isEmpty(list)) {
                CouponBeforeGiveAddResponse couponBeforeGiveAddResponse = new CouponBeforeGiveAddResponse();
                CouponBeforeGiveEntity entity1 = list.get(0);
                BeanUtils.copyProperties(entity1, couponBeforeGiveAddResponse, "id");
                couponBeforeGiveAddResponse.setId(NumberConvertUtils.numberConvertToShortStr(entity1.getId()));
                return couponBeforeGiveAddResponse;
            }
        }
        CouponBeforeGiveAddResponse couponBeforeGiveAddResponse = new CouponBeforeGiveAddResponse();

        final Boolean result = this.save(entity);
        if (result) {
            try {
                couponBeforeGiveCacheService.refreshCouponBeforeGiveCache(entity.getId());
            } catch (BusinessException e) {
                log.error("添加转赠前的优惠券信息时，刷新缓存出错:", e);
            }
            BeanUtils.copyProperties(entity, couponBeforeGiveAddResponse, "id");
            couponBeforeGiveAddResponse.setId(NumberConvertUtils.numberConvertToShortStr(entity.getId()));
        }

        String url = urlShortBase + RestConstant.GET_URL_SHORT + couponBeforeGiveAddResponse.getId();

        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(couponEntity.getCouponThemeId());
        CouponUserEntity couponUserEntity = couponUserService.getById(couponEntity.getId());
        CouponBeforeGiveAddMessageBo couponBeforeGiveAddMessageBo = new CouponBeforeGiveAddMessageBo();
        couponBeforeGiveAddMessageBo.setCouponDiscountType(couponThemeCache.getCouponDiscountType());
        couponBeforeGiveAddMessageBo.setCouponValue(couponThemeCache.getDiscountAmount());
        couponBeforeGiveAddMessageBo.setGiveUserMobile(couponUserEntity.getBindTel());
        couponBeforeGiveAddMessageBo.setGiveUserName(entity.getGiveNickname());
        couponBeforeGiveAddMessageBo.setReceiveUserMobile(bo.getReceiverMobile());
        couponBeforeGiveAddMessageBo.setUrl(url);

        //组装短信信息
        Map<String, String> messageParameter = getMessageParameter(couponBeforeGiveAddMessageBo);
        couponBeforeGiveAddResponse.setParameter(messageParameter);
        //短信
        if (Objects.equals(CouponGiveTypeEnum.TYPE_SMS.getType(), bo.getGiveType())) {
            sendDonateMessage(couponBeforeGiveAddMessageBo.getReceiveUserMobile(), messageParameter);
        }

        return couponBeforeGiveAddResponse;
    }

    /**
     * 短信消息放入kafka
     *
     * @param receiveUserMobile
     * @param content
     */
    private void sendDonateMessage(String receiveUserMobile, Map<String, String> content) {

        List<Map<String, String>> receiver = new ArrayList<>();
        Map<String, String> receiverMap = new HashMap<>();
        receiverMap.put("phone", receiveUserMobile);
        receiver.add(receiverMap);
        log.info("sms-content:{}", content);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("event", COUPON_TRANSFER_CODE);
        jsonObject.put("receiver", receiver);
        Map<String, Map<String, String>> sms = new HashMap();
        sms.put("SMS", content);
        jsonObject.put("messageParam", sms);

        try {
            log.info("短信消息data:" + jsonObject.toJSONString());
            kafkaTemplate.send("TRIGGER_EVENTSYSTEM-TOPIC", jsonObject.toJSONString());
        } catch (Exception ex) {
            log.error("短信发送出错:", ex);
        }
    }

    /**
     * 检查参数合法性
     */
    private void validateParam(CouponBeforeGiveAddBo bo) {

        if (Objects.isNull(bo.getCouponId())) {
            throw new BusinessException(CouponBeforeGiveErrorCode.COUPON_ID_NOT_EMPTY);
        }

        //校验转赠类型
        if (!CouponGiveTypeEnum.contains(bo.getGiveType())) {
            throw new BusinessException(CouponBeforeGiveErrorCode.GIVETYPE_NOT_EXIST);
        }

        //平台类型
        if (!ClientTypeEnum.contains(bo.getClientType())) {
            throw new BusinessException(CouponBeforeGiveErrorCode.CLIENTTYPE_NOT_EXIST);
        }

        if (Objects.equals(CouponGiveTypeEnum.TYPE_SMS.getType(), bo.getGiveType())) {
            final Integer count = getCouponBeforeGiveCount(bo.getCouponId(), CouponGiveTypeEnum.TYPE_SMS.getType());
            if (giveTypeSmsLimit.compareTo(count) <= 0) {
                throw new BusinessException(CouponBeforeGiveErrorCode.SMS_REACH_THE_UPPER_LIMIT);
            }
        }
    }

    /**
     *数据初始化
     * @param bo
     * @throws Exception
     */
    private CouponEntity initDtoCommon(CouponBeforeGiveAddBo bo, CouponBeforeGiveEntity entity) {


        AppUserInfo userInfo = AppAuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        //端用户需要校验验证码
        if (StringUtils.equals(bo.getClientType(), ClientTypeEnum.B.getKey())) {

            if (Objects.equals(CouponGiveTypeEnum.TYPE_SMS.getType(), bo.getGiveType()) && StringUtils.isBlank(bo.getReceiverMobile())) {
                throw new BusinessException(CouponBeforeGiveErrorCode.COUPON_GIVE_MOBILE_CAN_NOT_EMPTY);
            }
            // 验证码校验参数
            CaptchasBo captchasBo = new CaptchasBo();
            captchasBo.setBusinessType(BusinessTypeEnum.COUPON_GIVE.getType());
            captchasBo.setCaptcha(bo.getCaptcha());
            captchasBo.setCaptchaType(bo.getCaptchaType());
            captchasBo.setCipherMobile(AESPromotionUtil.encrypt(userInfo.getUserMobile()));
            captchasBo.setDeviceId(bo.getDeviceId());
            captchasBo.setMobile(userInfo.getUserMobile());
            //查询是否过期
             Boolean isExpire = captchasService.queryCaptchas(captchasBo);
            if (isExpire) {
                captchasService.verifyCaptchas(captchasBo);
            }
        }
        LambdaQueryWrapper<CouponEntity> queryWrapper = Wrappers.lambdaQuery(CouponEntity.class);
        queryWrapper.eq(CouponEntity::getId, bo.getCouponId()).eq(CouponEntity::getUserId, userInfo.getUserId()).
                eq(CouponEntity::getUserType, userInfo.getUserType()).eq(CouponEntity::getIsDeleted, YesNoEnum.NO.getValue());

        CouponEntity couponEntity = couponService.getOne(queryWrapper);
        if (Objects.isNull(couponEntity)) {
            throw new BusinessException(CouponBeforeGiveErrorCode.COUPON_NOT_EXIST);
        }

        // 校验转赠限制
        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(couponEntity.getCouponThemeId());
        boolean canDonate = Objects.equals(couponThemeCache.getCanDonation(), 1);
        boolean canAssign = Objects.equals(couponThemeCache.getCanTransfer(), 1);
        boolean couponGive = Objects.equals(couponEntity.getSource(), CouponSourceTypeEnum.COUPON_SOURCE_PRESENTED.getSource());
        if ((!canDonate && !canAssign) || couponGive){
            throw new BusinessException(CouponBeforeGiveErrorCode.COUPON_CAN_NOT_GIVE);
        }

        if (!Objects.equals(couponEntity.getStatus(), CouponStatusEnum.STATUS_USE.getStatus())) {
            throw new BusinessException(CouponBeforeGiveErrorCode.COUPON_CAN_NOT_GIVE);
        }

        entity.setCouponId(couponEntity.getId());
        entity.setCouponThemeId(couponEntity.getCouponThemeId());
        entity.setGiveUserid(userInfo.getUserId());
        entity.setGiveNickname(userInfo.getNickName());
        entity.setGiveType(bo.getGiveType());
        entity.setGiveUserType(couponEntity.getUserType());
        entity.setGiveAvatar(bo.getGiveAvatar());
        entity.setGiveUserMobile(userInfo.getUserMobile());
        entity.setTerminalType(bo.getTerminalType());
        entity.setIsDeleted(0);
        entity.setVersionNo(0);
        Date nowTime = new Date();
        entity.setCreateTime(nowTime);
        entity.setExpireTime(DateUtil.parse(DateUtil.format(nowTime, "yyyy-MM-dd") + " 23:59:59", "yyyy-MM-dd HH:mm:ss"));
        entity.setUpdateTime(nowTime);
        entity.setUpdateUserid(userInfo.getUserId());
        entity.setUpdateUsername(userInfo.getUserName());
        entity.setReceiveUserMobile(bo.getReceiverMobile());
        //B端用户获取获取头像
        if (StringUtils.equals(bo.getClientType(), ClientTypeEnum.B.getKey())) {
            ProfileDto profileDto = profileFeignClient.getBrokerProfile(bo.getUserToken(), userInfo.getUnionId(), bo.getTerminalType());
            if (Objects.isNull(profileDto)) {
                throw new BusinessException(CommonErrorCode.NO_LOGIN);
            }
            entity.setGiveAvatar(profileDto.getAvatar());
            entity.setGiveNickname(StringUtils.isNotEmpty(profileDto.getName()) ? profileDto.getName() : profileDto.getNickname());
        }
        return couponEntity;

    }


    private Map<String, String> getMessageParameter(CouponBeforeGiveAddMessageBo bo) {
        // 姓名
        String name = java.util.Optional.ofNullable(bo).map(CouponBeforeGiveAddMessageBo::getGiveUserName).orElse("");
        if (StringUtils.isNotBlank(name)) {
            name = StringUtils.rightPad(StringUtils.left(name, 1), StringUtils.length(name), "*");
        } else {
            // 兼容机构经纪人的账号，不脱敏
            if (MobileValidateUtil.isMobile(bo.getGiveUserMobile())) {
                name = StringUtils.left(bo.getGiveUserMobile(), 3)
                        .concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(bo.getGiveUserMobile(), 4),
                                StringUtils.length(bo.getGiveUserMobile()), "*"), "***"));
            } else {
                name = bo.getGiveUserMobile();
            }
        }

        // 面值
        BigDecimal couponValue = bo.getCouponValue();
        String value = "";
        if (Objects.equals(bo.getCouponDiscountType(), CouponDiscountType.DISCOUNT.getType())) {
            //折扣券数值除100
            BigDecimal divisor2 = new BigDecimal(100);
            BigDecimal discountVal = couponValue.divide(divisor2, 2, BigDecimal.ROUND_HALF_UP);

            DecimalFormat df = new DecimalFormat("#.##");
            value = df.format(discountVal.doubleValue()) + "折";
        } else {
            //金额券保留小数点后两位小数,舍弃后边数字
            DecimalFormat df = new DecimalFormat("#.##");
            String str3 = df.format(couponValue.doubleValue());
            value = str3 + "元";
        }

        Map<String, String> content = new HashMap<>();
        content.put("var1", name);
        content.put("var2", value);
        content.put("var3", bo.getUrl());
        return content;
    }

    @Override
    public Integer getCouponBeforeGiveCanSendSmsCount(Long couponId){
        if (Objects.isNull(couponId)) {
            throw new BusinessException(CouponBeforeGiveErrorCode.COUPON_ID_NOT_EMPTY);
        }
        LambdaQueryWrapper<CouponBeforeGiveEntity> queryWrapper = Wrappers.lambdaQuery(CouponBeforeGiveEntity.class);
        queryWrapper.eq(CouponBeforeGiveEntity::getCouponId, couponId).eq(CouponBeforeGiveEntity::getGiveType, CouponGiveTypeEnum.TYPE_SMS.getType());
        Integer count = this.count(queryWrapper);
        if (giveTypeSmsLimit.compareTo(count) > 0) {
            return giveTypeSmsLimit - count;
        }
        return 0;
    }

    private Integer getCouponBeforeGiveCount(Long couponId, Integer giveType) {
        LambdaQueryWrapper<CouponBeforeGiveEntity> queryWrapper = Wrappers.lambdaQuery(CouponBeforeGiveEntity.class);
        queryWrapper.eq(CouponBeforeGiveEntity::getCouponId, couponId).eq(CouponBeforeGiveEntity::getGiveType, giveType);
        final Integer count = this.count(queryWrapper);
        return count;
    }
}
