package com.fcb.coupon.app.facade.impl;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.app.facade.ClientUserFacade;
import com.fcb.coupon.app.infra.inteceptor.AppUserInfo;
import com.fcb.coupon.app.remote.dto.BrokerInfoDto;
import com.fcb.coupon.app.remote.dto.input.*;
import com.fcb.coupon.app.remote.dto.output.*;
import com.fcb.coupon.app.remote.user.CustomerFeignClient;
import com.fcb.coupon.app.remote.user.MemberFeignClient;
import com.fcb.coupon.app.remote.user.SaasFeignClient;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.RedisCacheKeyConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.dto.SaasUserLoginChectDto;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.enums.YesNoEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.redisson.codec.FastJsonCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author YangHanBin
 * @date 2021-08-23 8:34
 */
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class ClientUserFacadeImpl implements ClientUserFacade {
    private final CustomerFeignClient customerFeignClient;
    private final MemberFeignClient memberFeignClient;
    private final RedissonClient redissonClient;
    private final SaasFeignClient saasFeignClient;

    // userInfo缓存时间 分钟
    private final int userInfoExpireMinutes = 5;


    @Override
    public AppUserInfo getSaasInfoByToken(String token) {
        SaasUserInfoInput saasUserInfoInput = new SaasUserInfoInput();
        saasUserInfoInput.setToken(token);
        SaasUserInfoOutput saasUserInfoOutput = saasFeignClient.getAgentInfoById(saasUserInfoInput);
        if (Objects.isNull(saasUserInfoOutput.getData())) {
            throw new BusinessException(CommonErrorCode.API_CALL_ERROR.getCode(), CommonErrorCode.API_CALL_ERROR.getMessage());
        }
        SaasUserInfoOutput.SaasUserInfo saasUserInfo = saasUserInfoOutput.getData();
        AppUserInfo appUserInfo = new AppUserInfo();
        appUserInfo.setUserId(saasUserInfo.getPersonId());
        appUserInfo.setUserName(saasUserInfo.getName());
        appUserInfo.setUserMobile(saasUserInfo.getPhone());
        appUserInfo.setUserType(UserTypeEnum.SAAS.getUserType());
        return appUserInfo;
    }

    @Override
    public AppUserInfo getMemberInfoByUnionId(String unionId) {
        return getMemberInfoByUnionId(unionId, true);
    }

    @Override
    public AppUserInfo getMemberInfoByUnionId(String unionId, boolean cacheUserInfo) {
        if (!cacheUserInfo) {
            return getMemberInfoByUnionIdInternal(unionId);
        }

        String cacheKey = MessageFormat.format(RedisCacheKeyConstant.UNION_USERINFO_CACHE, unionId, UserTypeEnum.B.getUserType());
        RBucket<AppUserInfo> rBucket = redissonClient.getBucket(cacheKey, FastJsonCodec.INSTANCE);
        AppUserInfo appUserInfo = rBucket.get();
        if (Objects.nonNull(appUserInfo)) {
            return appUserInfo;
        }
        appUserInfo = getMemberInfoByUnionIdInternal(unionId);
        rBucket.set(appUserInfo, userInfoExpireMinutes, TimeUnit.MINUTES);
        return appUserInfo;
    }

    @Override
    public AppUserInfo getCustomerInfoByUnionId(String unionId) {
        return getCustomerInfoByUnionId(unionId, true);
    }

    @Override
    public AppUserInfo getCustomerInfoByUnionId(String unionId, boolean cacheUserInfo) {
        if (!cacheUserInfo) {
            return getCustomerInfoByUnionIdInternal(unionId);
        }
        String cacheKey = MessageFormat.format(RedisCacheKeyConstant.UNION_USERINFO_CACHE, unionId, UserTypeEnum.C.getUserType());
        RBucket<AppUserInfo> rBucket = redissonClient.getBucket(cacheKey, FastJsonCodec.INSTANCE);
        AppUserInfo appUserInfo = rBucket.get();
        if (Objects.nonNull(appUserInfo)) {
            return appUserInfo;
        }
        appUserInfo = getCustomerInfoByUnionIdInternal(unionId);
        rBucket.set(appUserInfo, userInfoExpireMinutes, TimeUnit.MINUTES);
        return appUserInfo;
    }

    /**
     * 校验C端用户登录
     *
     * @param request request
     */
    @Override
    public Boolean validateCustomerLogin(HttpServletRequest request) {
        HashMap<String, String> headers = getHeaders(request);
        ResponseDto<String> responseDto = customerFeignClient.innerCheckAccessToken(Collections.emptyMap(), headers);
        return CouponConstant.SUCCESS_CODE.equals(responseDto.getCode());
    }

    /**
     * 校验B端用户登录
     *
     * @param request request
     */
    @Override
    public Boolean validateMemberLogin(HttpServletRequest request) {
        HashMap<String, String> headers = getHeaders(request);
        ResponseDto<Void> responseDto = memberFeignClient.innerCheckAccessToken(Collections.emptyMap(), headers);
        return CouponConstant.SUCCESS_CODE.equals(responseDto.getCode());
    }

    @Override
    public Boolean validateSaasLogin(HttpServletRequest request) {
        SaasUserInfoInput param = new SaasUserInfoInput();
        param.setToken(request.getHeader("authorization"));
        ResponseDto<SaasUserLoginChectDto> responseDto = saasFeignClient.checkTokenValid4saas(param);
        if (Objects.isNull(responseDto) || Objects.isNull(responseDto.getData()) || Objects.equals(responseDto.getData().getTokenValid(), YesNoEnum.NO.getValue())) {
            return false;
        }
        return true;
    }

    @Override
    public AppUserInfo getCustomerInfoByHdTokenAndTerminalType(String hdToken, String terminalType) {
        if (StringUtils.isBlank(terminalType)) {
            terminalType = "web";
        }
        CustomerHdTokenInfoInput input = new CustomerHdTokenInfoInput();
        input.setHdToken(hdToken);
        input.setTerminalType(terminalType);
        ResponseDto<CustomerUserInfo> response = customerFeignClient.getCustomerInfoByHdTokenAndTerminalType(input);
        if (!CouponConstant.SUCCESS_CODE.equals(response.getCode())) {
            throw new BusinessException(CommonErrorCode.API_CALL_ERROR.getCode(), response.getMessage());
        }
        CustomerUserInfo customerUserInfo = response.getData();

        AppUserInfo userInfo = new AppUserInfo();
        userInfo.setUserId(customerUserInfo.getUserId());
        userInfo.setUserName(customerUserInfo.getName());
        userInfo.setUserMobile(customerUserInfo.getPhone());
        userInfo.setNickName(customerUserInfo.getNickName());
        userInfo.setUnionId(customerUserInfo.getUnionId());
        userInfo.setUserType(UserTypeEnum.C.getUserType());
        return userInfo;
    }

    @Override
    public AppUserInfo getMemberInfoByHdTokenAndTerminalType(String hdToken, String terminalType) {
        if (StringUtils.isBlank(terminalType)) {
            terminalType = "web";
        }
        MemberHdTokenInfoInput input = new MemberHdTokenInfoInput();
        input.setHdToken(hdToken);
        input.setTerminalType(terminalType);
        ResponseDto<MemberUserInfo> response = memberFeignClient.getMemberInfoByHdTokenAndTerminalType(input);
        if (!CouponConstant.SUCCESS_CODE.equals(response.getCode())) {
            throw new BusinessException(CommonErrorCode.API_CALL_ERROR.getCode(), response.getMessage());
        }
        MemberUserInfo memberUserInfo = response.getData();

        AppUserInfo userInfo = new AppUserInfo();
        userInfo.setUserId(memberUserInfo.getUserId());
        userInfo.setUserName(memberUserInfo.getName());
        userInfo.setUserMobile(memberUserInfo.getPhone());
        userInfo.setNickName(memberUserInfo.getNickName());
        userInfo.setUnionId(memberUserInfo.getUnionId());
        userInfo.setUserType(UserTypeEnum.B.getUserType());
        return userInfo;
    }

    @Override
    public AppUserInfo getCustomerInfoByPhone(String phone) {
        String cacheKey = MessageFormat.format(RedisCacheKeyConstant.PHONE_USERINFO_CACHE, phone, UserTypeEnum.C.getUserType());
        RBucket<AppUserInfo> rBucket = redissonClient.getBucket(cacheKey, FastJsonCodec.INSTANCE);
        AppUserInfo appUserInfo = rBucket.get();
        if (Objects.nonNull(appUserInfo)) {
            return appUserInfo;
        }
        appUserInfo = getCustomerInfoByPhoneInternal(phone);
        if (appUserInfo == null) {
            rBucket.set(new AppUserInfo(), 3, TimeUnit.MINUTES);
        } else {
            rBucket.set(appUserInfo, userInfoExpireMinutes, TimeUnit.MINUTES);
        }

        return appUserInfo;
    }

    @Override
    public AppUserInfo getMemberInfoByPhone(String phone) {
        String cacheKey = MessageFormat.format(RedisCacheKeyConstant.PHONE_USERINFO_CACHE, phone, UserTypeEnum.B.getUserType());
        RBucket<AppUserInfo> rBucket = redissonClient.getBucket(cacheKey, FastJsonCodec.INSTANCE);
        AppUserInfo appUserInfo = rBucket.get();
        if (Objects.nonNull(appUserInfo)) {
            return appUserInfo;
        }
        appUserInfo = getMemberInfoByPhoneInternal(phone);
        if (appUserInfo == null) {
            rBucket.set(new AppUserInfo(), 3, TimeUnit.MINUTES);
        } else {
            rBucket.set(appUserInfo, userInfoExpireMinutes, TimeUnit.MINUTES);
        }

        return appUserInfo;
    }

    private AppUserInfo getMemberInfoByPhoneInternal(String phone) {
        List<BrokerInfoSimpleDto> cUserInfoList = listMemberInfoByPhones(Collections.singletonList(phone));
        if (cUserInfoList == null) {
            return null;
        }
        BrokerInfoSimpleDto output = cUserInfoList.get(0);
        return new AppUserInfo()
                .setUserId(output.getBrokerId())
                .setUserName(output.getNickName())
                .setUserMobile(output.getPhoneNo())
                .setNickName(output.getNickName())
                .setUnionId(output.getUnionId())
                .setUserType(UserTypeEnum.B.getUserType());

    }

    @Override
    public List<BrokerInfoSimpleDto> listMemberInfoByPhones(List<String> phoneList) {
        BrokerInfoSimpleInputDto input = new BrokerInfoSimpleInputDto();
        input.setPhoneNoList(phoneList);
        ResponseDto<List<BrokerInfoSimpleDto>> responseDto = memberFeignClient.getBrokerInfoListByPhones(input);
        List<BrokerInfoSimpleDto> bUserInfoList = responseDto.getData();
        if (CollectionUtils.isEmpty(bUserInfoList)) {
            log.error("根据电话号码查询C端用户信息 null input={}", JSON.toJSONString(input));
            return null;
        }
        return bUserInfoList;
    }

    @Override
    public List<CustomerInfoSimpleOutput> listCustomerInfoByPhones(List<String> phones) {
        CustomerInfoSimpleInput input = new CustomerInfoSimpleInput();
        input.setPhoneNoList(phones);
        ResponseDto<List<CustomerInfoSimpleOutput>> listResponseDto = customerFeignClient.listCustomerInfoByPhones(input);
        List<CustomerInfoSimpleOutput> cUserInfoList = listResponseDto.getData();
        if (CollectionUtils.isEmpty(cUserInfoList)) {
            log.error("根据电话号码查询C端用户信息 null input={}", JSON.toJSONString(input));
            return null;
        }
        return cUserInfoList;
    }

    private AppUserInfo getCustomerInfoByPhoneInternal(String phone) {
        List<CustomerInfoSimpleOutput> cUserInfoList = listCustomerInfoByPhones(Collections.singletonList(phone));
        if (cUserInfoList == null) {
            return null;
        }
        CustomerInfoSimpleOutput output = cUserInfoList.get(0);
        return new AppUserInfo()
                .setUserId(output.getCustomerId())
                .setUserName(output.getNickName())
                .setUserMobile(output.getPhoneNo())
                .setNickName(output.getNickName())
                .setUnionId(output.getUnionId())
                .setUserType(UserTypeEnum.C.getUserType());

    }

    /**
     * 根据unionId获取C端用户信息，没有缓存
     *
     * @param unionId unionId
     * @return AppUserInfo
     */
    private AppUserInfo getCustomerInfoByUnionIdInternal(String unionId) {
        ResponseDto<CustomerByUnionIdOutput> customerResponse = customerFeignClient.getCustomerInfoByUnionId(new CustomerByUnionIdInput().setUnionId(unionId));
        if (!CouponConstant.SUCCESS_CODE.equals(customerResponse.getCode())) {
            throw new BusinessException(CommonErrorCode.API_CALL_ERROR.getCode(), customerResponse.getMessage());
        }
        CustomerByUnionIdOutput out = customerResponse.getData();

        AppUserInfo userInfo = new AppUserInfo();
        userInfo.setUserId(out.getCustomerId());
        userInfo.setUserName(out.getName());
        userInfo.setUserMobile(out.getMphone());
        userInfo.setUserType(UserTypeEnum.C.getUserType());
        return userInfo;
    }

    /**
     * 根据unionId获取B端用户信息，没有缓存
     *
     * @param unionId unionId
     * @return AppUserInfo
     */
    private AppUserInfo getMemberInfoByUnionIdInternal(String unionId) {
        BrokerInfoByUnionIdInput input = new BrokerInfoByUnionIdInput();
        input.setUnionId(unionId);
        ResponseDto<BrokerInfoDto> memberResponse = memberFeignClient.findBrokerInfoByUnionId(input);
        if (!CouponConstant.SUCCESS_CODE.equals(memberResponse.getCode())) {
            throw new BusinessException(CommonErrorCode.API_CALL_ERROR.getCode(), memberResponse.getMessage());
        }
        BrokerInfoDto out = memberResponse.getData();

        AppUserInfo userInfo = new AppUserInfo();
        userInfo.setUserId(out.getBrokerId());
        userInfo.setUserName(out.getName());
        userInfo.setUserMobile(out.getMphone());
        userInfo.setUserType(UserTypeEnum.B.getUserType());
        return userInfo;
    }

    private HashMap<String, String> getHeaders(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization)) {
            throw new BusinessException(CommonErrorCode.AUTHORIZATION_NULL);
        }
        String unionId = request.getHeader("unionId");
        if (StringUtils.isBlank(unionId)) {
            throw new BusinessException(CommonErrorCode.UNION_ID_NULL);
        }
        String terminalType = request.getHeader("terminalType");
        if (StringUtils.isBlank(terminalType)) {
            terminalType = "web";
        }
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", authorization);
        headerMap.put("unionId", unionId);
        headerMap.put("terminalType", terminalType);
        return headerMap;
    }


}
