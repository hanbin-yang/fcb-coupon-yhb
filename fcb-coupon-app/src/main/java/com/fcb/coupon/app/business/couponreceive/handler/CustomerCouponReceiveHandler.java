package com.fcb.coupon.app.business.couponreceive.handler;

import com.alibaba.fastjson.JSONArray;
import com.fcb.coupon.app.exception.CouponReceiveErrorCode;
import com.fcb.coupon.app.infra.inteceptor.AppUserInfo;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.remote.dto.input.CustomerInfoSimpleInput;
import com.fcb.coupon.app.remote.dto.output.CustomerInfoSimpleOutput;
import com.fcb.coupon.app.remote.user.CustomerFeignClient;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * C端用户领券处理程序
 * @author YangHanBin
 * @date 2021-08-16 9:43
 */
@Component
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class CustomerCouponReceiveHandler extends AbstractCouponReceiveHandler {
    private final CustomerFeignClient customerFeignClient;

    @Override
    public boolean supports(Integer userType) {
        return UserTypeEnum.C.getUserType().equals(userType);
    }

    @Override
    public void validateApplicableUserType(CouponThemeCache couponTheme) {
        JSONArray applicableUserTypes = getCouponThemeApplicableUserTypes(couponTheme.getApplicableUserTypes());
        if (!applicableUserTypes.contains(UserTypeEnum.C.getUserType())) {
            throw new BusinessException(CouponReceiveErrorCode.NOT_SUPPORT_C);
        }
    }

    @Override
    protected AppUserInfo getUserInfoByMobile(String mobile) {
        CustomerInfoSimpleInput input = new CustomerInfoSimpleInput();
        input.setPhoneNoList(Collections.singletonList(mobile));
        ResponseDto<List<CustomerInfoSimpleOutput>> responseDto = customerFeignClient.listCustomerInfoByPhones(input);
        if (!CouponConstant.SUCCESS_CODE.equals(responseDto.getCode())) {
            throw new BusinessException(CommonErrorCode.API_CALL_ERROR.getCode(), responseDto.getMessage());
        }
        List<CustomerInfoSimpleOutput> list = responseDto.getData();
        CustomerInfoSimpleOutput out = list.get(0);

        AppUserInfo appUserInfo = new AppUserInfo();

        return appUserInfo
                .setUserId(out.getCustomerId())
                .setUserName(out.getNickName())
                .setUserMobile(out.getPhoneNo())
                .setUserType(UserTypeEnum.B.getUserType())
                .setUnionId(out.getUnionId())
                ;
    }
}
