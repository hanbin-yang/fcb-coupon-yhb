package com.fcb.coupon.app.business.couponreceive.handler;

import com.alibaba.fastjson.JSONArray;
import com.fcb.coupon.app.exception.CouponReceiveErrorCode;
import com.fcb.coupon.app.infra.inteceptor.AppUserInfo;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.remote.dto.input.BrokerInfoSimpleInputDto;
import com.fcb.coupon.app.remote.dto.output.BrokerInfoSimpleDto;
import com.fcb.coupon.app.remote.user.MemberFeignClient;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.enums.YesNoEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * B端用户领券处理程序
 * @author YangHanBin
 * @date 2021-08-17 14:06
 */
@Component
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class MemberCouponReceiveHandler extends AbstractCouponReceiveHandler {
    private final MemberFeignClient memberFeignClient;

    @Override
    public boolean supports(Integer userType) {
        return UserTypeEnum.B.getUserType().equals(userType);
    }

    @Override
    public void validateApplicableUserType(CouponThemeCache couponTheme) {
        JSONArray applicableUserTypes = getCouponThemeApplicableUserTypes(couponTheme.getApplicableUserTypes());
        if (!applicableUserTypes.contains(UserTypeEnum.B.getUserType()) &&
                Objects.equals(couponTheme.getCanDonation(), YesNoEnum.NO.getValue())) {
            throw new BusinessException(CouponReceiveErrorCode.NOT_SUPPORT_B);
        }
    }

    @Override
    protected AppUserInfo getUserInfoByMobile(String mobile) {
        BrokerInfoSimpleInputDto input = new BrokerInfoSimpleInputDto();
        input.setPhoneNoList(Collections.singletonList(mobile));
        ResponseDto<List<BrokerInfoSimpleDto>> responseDto = memberFeignClient.getBrokerInfoListByPhones(input);
        if (!CouponConstant.SUCCESS_CODE.equals(responseDto.getCode())) {
            throw new BusinessException(CommonErrorCode.API_CALL_ERROR.getCode(), responseDto.getMessage());
        }

        BrokerInfoSimpleDto outInfo = responseDto.getData().get(0);

        AppUserInfo appUserInfo = new AppUserInfo();

        return appUserInfo
                .setUserId(outInfo.getBrokerId())
                .setUserName(outInfo.getNickName())
                .setUserMobile(outInfo.getPhoneNo())
                .setUserType(UserTypeEnum.B.getUserType())
                .setUnionId(outInfo.getUnionId())
                ;
    }
}
