package com.fcb.coupon.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.app.exception.CaptchasErrorCode;
import com.fcb.coupon.app.mapper.CaptchasMapper;
import com.fcb.coupon.app.model.bo.CaptchasBo;
import com.fcb.coupon.app.model.entity.CaptchasEntity;
import com.fcb.coupon.app.properties.CaptchasProperties;
import com.fcb.coupon.app.remote.dto.BrokerInfoDto;
import com.fcb.coupon.app.remote.dto.input.BrokerInfoByUnionIdInput;
import com.fcb.coupon.app.remote.user.CaptchasFeignClient;
import com.fcb.coupon.app.remote.user.MemberFeignClient;
import com.fcb.coupon.app.service.CaptchasService;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.RedisCacheKeyConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.BusinessTypeEnum;
import com.fcb.coupon.common.enums.ClientTypeEnum;
import com.fcb.coupon.common.enums.YesNoEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.HessianCodecUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 验证码处理
 * @author mashiqiong
 * @date 2021-8-19 10:12
 */
@Service
@RefreshScope
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class CaptchasServiceImpl extends ServiceImpl<CaptchasMapper, CaptchasEntity>  implements CaptchasService {
    private static final String CAPTCHA_TYPE_VOICE = "voice";

    private final RedissonClient redissonClient;
    private final KafkaTemplate kafkaTemplate;
    private final MemberFeignClient memberFeignClient;
    private final CaptchasFeignClient captchasFeignClient;
    private final CaptchasProperties captchasProperties;

    /**
     * 校验验证码是否有效
     *
     * @param bo
     */
    @Override
    public void verifyCaptchas(CaptchasBo bo) {
        Optional<BusinessTypeEnum> businessTypeEnum = BusinessTypeEnum.valueOf(bo.getBusinessType());
        Integer businessType = businessTypeEnum.map(BusinessTypeEnum::getType).orElseThrow(() -> new BusinessException(CaptchasErrorCode.CAPTCHAS_UN_SUPPORTED_BUSINESS.getCode(), CaptchasErrorCode.CAPTCHAS_UN_SUPPORTED_BUSINESS.getMessage()));

        CaptchasEntity captchasEntity = new CaptchasEntity();
        captchasEntity.setDeviceId(bo.getDeviceId());
        captchasEntity.setMobile(bo.getCipherMobile());
        captchasEntity.setExpireTime(new Date());
        captchasEntity.setSuccessIs(YesNoEnum.YES.getValue());
        captchasEntity.setBusinessType(businessType);

        String cacheKey = String.format(RedisCacheKeyConstant.MOBILE_CAPTCHAS_FAIL,businessType, bo.getDeviceId(), bo.getMobile());
        RBucket<Integer> bucket = redissonClient.getBucket(cacheKey);
        Integer failCount  = bucket.get();
        if (failCount == null) {
            failCount = 0;
        }

        Integer maxMobileCaptchasFailCount = getMaxMobileCaptchasFailCount();
        if (failCount >= maxMobileCaptchasFailCount) {
            //如果超过了最大次数，清空该手机号下的所有验证码
            this.baseMapper.updateByMobile(captchasEntity);
            throw new BusinessException(CaptchasErrorCode.CAPTCHAS_OBTAIN_CODE_AGAIN);
        }

        captchasEntity.setCaptcha(bo.getCaptcha());
        int count = this.baseMapper.updateByMobile(captchasEntity);
        bucket.delete();
        if (count<=0) {
            failCount++;
            bucket.set(failCount);
            if (failCount >= maxMobileCaptchasFailCount) {
                //最后一次失败就直接提示重新获取验证码
                this.baseMapper.updateByMobile(captchasEntity);
                throw new BusinessException(CaptchasErrorCode.CAPTCHAS_OBTAIN_CODE_AGAIN);
            }
            throw new BusinessException(CaptchasErrorCode.CAPTCHAS_DISABLED);
        } else {
            Integer validHourTime = captchasProperties.getValidHourTime();
            if (validHourTime == null) {
                validHourTime = 1;
            }

            //设置一个业务过期时间供前端使用
            cacheKey = String.format(RedisCacheKeyConstant.MOBILE_CAPTCHAS_EXPIRE,businessType, bo.getDeviceId(), bo.getMobile());
            bucket = redissonClient.getBucket(cacheKey);
            bucket.set(0);
            bucket.expire(validHourTime * 60, TimeUnit.MINUTES);
        }
    }

    /**
     * 获取验证码，最大可失败次数
     * 从配置文件中获取key(maxMobileCaptchasFailCount)
     * 从配置文件中获取key(maxOrderMobileCaptchasFailCount)
     * 如果从配置文件中不能获取到，默认为3次
     * @return
     */
    private Integer getMaxMobileCaptchasFailCount(){
        Integer maxFailCount = captchasProperties.getMaxMobileCaptchasFailCount();
        return maxFailCount == null ? 3 : maxFailCount;
    }

    /**
     * 发送验证码
     *
     * @param bo
     * @return string code
     */
    @Override
    public void sendCaptchasWithTx(CaptchasBo bo) {
        String cipherMobile = bo.getCipherMobile();
        if (StringUtils.isEmpty(cipherMobile)) {
            log.error("手机号码不能为空!");
            throw new BusinessException(CaptchasErrorCode.CAPTCHAS_MOBILE_CANNOT_BE_EMPTY);
        }

        //校验输入的手机号码与登录人的手机号是否一致
        checkMobileUniformity(bo);

        //验证码类型
        Optional<BusinessTypeEnum> businessTypeEnum = BusinessTypeEnum.valueOf(bo.getBusinessType());
        Integer businessType = businessTypeEnum.map(BusinessTypeEnum::getType).orElseThrow(() ->
                new BusinessException(CaptchasErrorCode.CAPTCHAS_UN_SUPPORTED_BUSINESS));

        //校验连续发送间隔
        checkSendTime(bo, businessType);

        //点获取验证码后，等待进行校验的有效时长（分钟）
        Integer expireMinTime = captchasProperties.getExpireMinTime();
        if (expireMinTime == null) {
            expireMinTime = 60;
        }
        String charValue = getString(6);
        //保存手机号。验证码。失效时间
        updateAndSave(bo, charValue, businessType, expireMinTime);
        log.info("发送验证码手机号:[{}],验证码类型:[{}] ",bo.getMobile() , bo.getCaptchaType());

        boolean success ;
        if (CAPTCHA_TYPE_VOICE.equalsIgnoreCase(bo.getCaptchaType())) {
            success = voiceSend(bo, charValue);
        } else {
            success = sendSms(bo, charValue, expireMinTime);
        }

        if (!success) {
            log.error("send sms fail");
        }
        String cacheKey = String.format(RedisCacheKeyConstant.MOBILE_CAPTCHAS_FAIL,businessType, bo.getDeviceId(), bo.getMobile());
        RBucket<Integer> bucket = redissonClient.getBucket(cacheKey);
        bucket.delete();
    }

    /**
     * 校验输入的手机号码与登录人的手机号是否一致
     * @param bo
     */
    private void checkMobileUniformity(CaptchasBo bo) {
        if (StringUtils.equals(bo.getClientType(), ClientTypeEnum.B.getKey()) && !StringUtils.isBlank(bo.getUserId())) {
            String mobilePhone = null;
            try {
                BrokerInfoDto memberInfo = getMemberInfoByUnionId(bo.getUserId());
                mobilePhone = memberInfo.getMphone();
            } catch (Exception e) {
                log.error("sendCaptchasWithTx 获取登录人信息出错!，error={}", e.getMessage(), e);
                throw new BusinessException(CaptchasErrorCode.CAPTCHAS_ERROR_LOGIN_INFORMATION);
            }

            if(!StringUtils.equals(bo.getMobile(), mobilePhone)) {
                log.error("sendCaptchasWithTx 输入的手机号码与登录人的手机号不一致!，mobile={}, mobilePhone={}", bo.getMobile(), mobilePhone);
                throw new BusinessException(CaptchasErrorCode.CAPTCHAS_MOBILE_INCONSISTENT);
            }

        }
    }

    /**
     * 校验连续发送间隔
     * @param bo
     * @param businessType
     */
    private void checkSendTime(CaptchasBo bo, Integer businessType) {
        //获取验证码配置
        Integer continuitySendTime = captchasProperties.getContinuitySendTime();
        if (continuitySendTime == null) {
            continuitySendTime = 60;
        }

        //校验连续发送间隔
        LambdaQueryWrapper<CaptchasEntity> queryWrapper = Wrappers.lambdaQuery(CaptchasEntity.class);
        queryWrapper.eq(CaptchasEntity::getMobile, bo.getCipherMobile())
                    .eq(CaptchasEntity::getBusinessType, businessType)
                    .ge(CaptchasEntity::getCreateTime, new Timestamp(System.currentTimeMillis() - 1000 * continuitySendTime));

        int createTimeCount = this.count(queryWrapper);
        if (createTimeCount >= 1) {
            throw new BusinessException(CaptchasErrorCode.CAPTCHAS_SEND_AT_MOST_ONE_PER_SECOND.getCode(),
                    String.format(CaptchasErrorCode.CAPTCHAS_SEND_AT_MOST_ONE_PER_SECOND.getMessage(), continuitySendTime));
        }
    }

    /**
     * 保存手机号。验证码。失效时间
     * @param bo
     * @param businessType
     * @param expireMinTime
     * @return
     */
    private void updateAndSave(CaptchasBo bo, String charValue, Integer businessType, Integer expireMinTime) {
        CaptchasEntity captchasEntity2 = new CaptchasEntity();
        captchasEntity2.setDeviceId(bo.getDeviceId());
        captchasEntity2.setMobile(bo.getCipherMobile());
        captchasEntity2.setSuccessIs(YesNoEnum.NO.getValue());
        captchasEntity2.setCaptcha(charValue);
        captchasEntity2.setExpireTime(new Timestamp(System.currentTimeMillis() + 1000 * 60 * expireMinTime));
        captchasEntity2.setBusinessType(businessType);
        captchasEntity2.setCreateTime(new Timestamp(System.currentTimeMillis()));
        //把之前的验证码设置为已验证
        this.baseMapper.disabledByMobile(captchasEntity2);
        //新增一条验证码记录
        save(captchasEntity2);
    }

    private boolean voiceSend(CaptchasBo bo, String charValue) {
        boolean success = false;
        // 将生成的验证码发送给短信服务端发送语音验证码
        log.info("send voice msg start ======== mobile:" + bo.getMobile() + ", cipherMobile :" + bo.getCipherMobile() + " charValue:" + charValue);
        String refId = captchasProperties.getVoiceRefId();
        Map<String, Object> params = new HashMap<>();
        params.put("mobile", bo.getMobile());
        params.put("message", charValue);
        // 设置唯一的业务ID
        params.put("refId", refId);

        String returnCode = null;
        try {
            // 调用短信接口发送语音验证码
            ResponseDto<Boolean> responseDto = captchasFeignClient.voiceSend(params);
            log.info("send voice msg ======== result" + responseDto);
            returnCode = responseDto.getCode();
        } catch (Exception ex) {
            log.error(" 调用短信接口发送语音验证码异常:", ex);
        }

        if (CouponConstant.SUCCESS_CODE.equals(returnCode)) {
            success = true;
        } else {
            log.info("send voice msg fail ======== mobile:" + bo.getCipherMobile() + ", charValue:" + charValue);
        }
        return success;
    }

    /**
     * 短信发送出
     * @param bo
     * @return
     */
    private Boolean sendSms(CaptchasBo bo, String charValue, Integer expireMinTime) {
        String nodeCode = StringUtils.equals(bo.getClientType(), ClientTypeEnum.C.getKey())?
             captchasProperties.getNodeCodeC():captchasProperties.getNodeCodeB();

        // 设置接收手机号
        Map<String, String> receiver = new HashMap<>();
        receiver.put("phone", bo.getMobile());
        List<Map<String, String>> receivers = new ArrayList<>();
        receivers.add(receiver);

        // 内容
        Map<String, String> param = new HashMap<>();
        // 验证码
        param.put("var1", charValue);
        // 分钟数
        param.put("var2", expireMinTime+"");
        Map<String, Map<String, String>> sms = new HashMap();
        sms.put("SMS", param);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("event", nodeCode);
        jsonObject.put("receiver", receiver);
        jsonObject.put("messageParam", sms);

        try {
            log.info("========短信消息data========: jsonObject={}, topic={}", jsonObject.toJSONString(), "VIP_TRIGGER_EVENTSYSTEM_TOPIC");
            kafkaTemplate.send("VIP_TRIGGER_EVENTSYSTEM_TOPIC", jsonObject.toJSONString());
        } catch (Exception ex) {
            log.error("========短信发送出错========:", ex);
            return false;
        }

        return true;
    }

    /**
     * 查询验证码是否存在
     *
     * @param bo
     */
    @Override
    public Boolean queryCaptchas(CaptchasBo bo) {
        Optional<BusinessTypeEnum> businessTypeEnum = BusinessTypeEnum.valueOf(bo.getBusinessType());
        Integer businessType = businessTypeEnum.map(BusinessTypeEnum::getType).orElseThrow(() -> new BusinessException(CaptchasErrorCode.CAPTCHAS_UN_SUPPORTED_BUSINESS.getCode(), CaptchasErrorCode.CAPTCHAS_UN_SUPPORTED_BUSINESS.getMessage()));

        String cacheKey = String.format(RedisCacheKeyConstant.MOBILE_CAPTCHAS_EXPIRE,businessType, bo.getDeviceId(), bo.getMobile());
        RBucket<Integer> rBucket = redissonClient.getBucket(cacheKey);
        return rBucket.get() == null ? true : false;
    }

    /**
     * 生成6位验证码
     * @param n
     * @return
     */
    private String getString(int n) {
        final Random random = new Random();
        // 生成6位验证码
        StringBuilder charValue = new StringBuilder();
        for (int i = 0; i < n; i++) {
            char c = (char) (random.nextInt(10) + '0');
            charValue.append(c);
        }
        return charValue.toString();
    }

    private BrokerInfoDto getMemberInfoByUnionId(String unionId) {
        BrokerInfoByUnionIdInput input = new BrokerInfoByUnionIdInput();
        input.setUnionId(unionId);
        ResponseDto<BrokerInfoDto> memberResponse = memberFeignClient.findBrokerInfoByUnionId(input);
        if (!CouponConstant.SUCCESS_CODE.equals(memberResponse.getCode())) {
            throw new BusinessException(memberResponse.getCode(), memberResponse.getMessage());
        }
        return memberResponse.getData();
    }
}
