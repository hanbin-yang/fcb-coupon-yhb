package com.fcb.coupon.backend.business.couponSend.handler;

import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.model.bo.CouponBatchSendBo;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.remote.client.BrokerClient;
import com.fcb.coupon.backend.remote.dto.input.BrokerIdInput;
import com.fcb.coupon.backend.remote.dto.input.BrokerInfoSimpleInputDto;
import com.fcb.coupon.backend.remote.dto.input.UnionIdRequest;
import com.fcb.coupon.backend.remote.dto.out.BrokerInfoByUnionIdResponse;
import com.fcb.coupon.backend.remote.dto.out.BrokerInfoDto;
import com.fcb.coupon.backend.remote.dto.out.BrokerInfoSimpleDto;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.enums.YesNoEnum;
import com.fcb.coupon.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 唐陆军
 * @Description 会员发券
 * @createTime 2021年08月05日 18:37:00
 */
@Slf4j
@Component
public class CouponSendBrokerHandler extends AbstractCouponSendHandler {

    @Autowired
    private BrokerClient brokerClient;

    @Override
    public Boolean supports(Integer sendUserType) {
        return UserTypeEnum.B.getUserType().equals(sendUserType);
    }

    /*
     * @description 验证发送类型
     * @author 唐陆军
     * @param: bo
     * @param: couponTheme
     * @date 2021-8-27 11:12
     */
    @Override
    protected void validateSendType(CouponBatchSendBo bo, CouponThemeEntity couponTheme) {
        Set<Integer> crowdScopeIdSet = getCrowdScopeIds(couponTheme.getApplicableUserTypes());
        if (CollectionUtils.isEmpty(crowdScopeIdSet)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_CONFIG_SEND_USER);
        }
        //可赠送也是可以发送的
        if (!crowdScopeIdSet.contains(bo.getSendUserType()) && YesNoEnum.NO.getValue().equals(couponTheme.getCanDonation())) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_SEND_USER_NOT_MATCH);
        }
    }

    /*
     * 填充发送内容
     */
    @Override
    protected void populateSendContext(List<CouponSendContext> sendContexts) {
        //根据userid获取会员信息
        Set<Long> brokerIdSet = sendContexts.stream().filter(m -> StringUtils.isNotBlank(m.getUserId())).map(m -> Long.valueOf(m.getUserId())).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(brokerIdSet)) {
            List<BrokerInfoDto> brokerInfoDtos = listBrokerById(new ArrayList<>(brokerIdSet));
            Map<String, BrokerInfoDto> brokerMap = brokerInfoDtos.stream().collect(Collectors.toMap(m -> m.getBrokerId(), m -> m));
            for (CouponSendContext sendContext : sendContexts) {
                BrokerInfoDto brokerInfo = brokerMap.get(sendContext.getUserId());
                if (brokerInfo == null) {
                    sendContext.error(false, "会员已注销或未注册");
                    continue;
                }
                sendContext.setBindTel(brokerInfo.getPhone());
            }
            return;
        }

        //根据手机号获取会员信息
        Set<String> phoneNoSet = sendContexts.stream().filter(m -> StringUtils.isNotBlank(m.getBindTel())).map(m -> m.getBindTel()).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(phoneNoSet)) {
            List<BrokerInfoSimpleDto> brokerInfoDtos = listBrokerByPhone(new ArrayList<>(phoneNoSet));
            Map<String, BrokerInfoSimpleDto> brokerMap = brokerInfoDtos.stream().collect(Collectors.toMap(m -> m.getPhoneNo(), m -> m));
            for (CouponSendContext sendContext : sendContexts) {
                BrokerInfoSimpleDto brokerInfo = brokerMap.get(sendContext.getBindTel());
                if (brokerInfo == null || StringUtils.isBlank(brokerInfo.getBrokerId())) {
                    sendContext.error(false, "会员已注销或未注册");
                    continue;
                }
                sendContext.setUserId(brokerInfo.getBrokerId());
            }
            return;
        }


        //根据uinonId获取会员信息(只有B端才有通过uionid发券)
        Set<String> unionIdSet = sendContexts.stream().filter(m -> StringUtils.isNotBlank(m.getUnionId())).map(m -> m.getUnionId()).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(unionIdSet)) {
            List<BrokerInfoByUnionIdResponse> brokerInfoDtos = listBrokerByUnionId(new ArrayList<>(unionIdSet));
            Map<String, BrokerInfoByUnionIdResponse> brokerMap = brokerInfoDtos.stream().collect(Collectors.toMap(m -> m.getMphone(), m -> m));
            for (CouponSendContext sendContext : sendContexts) {
                BrokerInfoByUnionIdResponse brokerInfo = brokerMap.get(sendContext.getUnionId());
                if (brokerInfo == null || StringUtils.isBlank(brokerInfo.getBrokerId())) {
                    sendContext.error(false, "会员已注销或未注册");
                    continue;
                }
                sendContext.setUserId(brokerInfo.getBrokerId());
            }
            return;
        }

        throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_SEND_USER_EMPTY);
    }


    private List<BrokerInfoSimpleDto> listBrokerByPhone(List<String> phoneNoList) {
        BrokerInfoSimpleInputDto input = new BrokerInfoSimpleInputDto();
        input.setPhoneNoList(phoneNoList);
        ResponseDto<List<BrokerInfoSimpleDto>> responseDto = brokerClient.getBrokerInfoListByPhones(input);
        if ("D00016".equalsIgnoreCase(responseDto.getCode())) {
            return Collections.EMPTY_LIST;
        }
        if (!CouponConstant.SUCCESS_CODE.equals(responseDto.getCode())) {
            throw new BusinessException(responseDto.getCode(), responseDto.getMessage());
        }
        return responseDto.getData();
    }

    private List<BrokerInfoDto> listBrokerById(List<Long> borkerIdList) {
        BrokerIdInput input = new BrokerIdInput();
        input.setBrokerIds(borkerIdList);
        ResponseDto<List<BrokerInfoDto>> responseDto = brokerClient.getBrokerInfoListByBrokerIds(input);
        if (!CouponConstant.SUCCESS_CODE.equals(responseDto.getCode())) {
            throw new BusinessException(responseDto.getCode(), responseDto.getMessage());
        }
        return responseDto.getData();
    }

    private List<BrokerInfoByUnionIdResponse> listBrokerByUnionId(List<String> unionIdList) {
        List<BrokerInfoByUnionIdResponse> borderList = new ArrayList<>(unionIdList.size());
        for (String unionId : unionIdList) {
            UnionIdRequest request = new UnionIdRequest();
            request.setUnionId(unionId);
            ResponseDto<BrokerInfoByUnionIdResponse> responseDto = brokerClient.findBrokerInfoByUnionId(request);
            //根据brokerId找不到对应的用户信息
            if ("D00016".equalsIgnoreCase(responseDto.getCode())) {
                continue;
            }
            if (!CouponConstant.SUCCESS_CODE.equals(responseDto.getCode())) {
                throw new BusinessException(responseDto.getCode(), responseDto.getMessage());
            }
            borderList.add(responseDto.getData());
        }
        return borderList;
    }

}
