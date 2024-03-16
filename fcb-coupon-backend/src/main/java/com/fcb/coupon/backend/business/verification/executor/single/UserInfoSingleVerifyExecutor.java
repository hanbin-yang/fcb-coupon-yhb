package com.fcb.coupon.backend.business.verification.executor.single;

import com.alibaba.fastjson.JSONArray;
import com.fcb.coupon.backend.business.verification.context.SingleVerifyContext;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;
import com.fcb.coupon.backend.model.dto.VerifyUserInfoDto;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * 用户校验相关执行器
 * @author YangHanBin
 * @date 2021-09-09 13:19
 */
public class UserInfoSingleVerifyExecutor extends AbstractSingleVerifyExecutor {
    public UserInfoSingleVerifyExecutor(SingleVerifyContext verifyContext, VerifyServiceContext serviceContext) {
        super(verifyContext, serviceContext);
    }

    @Override
    protected void doExecute() {
        validate();
    }

    @Override
    protected void after() {
        StoreInfoSingleVerifyExecutor delegate = new StoreInfoSingleVerifyExecutor(getVerifyContext(), getServiceContext());
        delegate.setCouponEsDoc(getCouponEsDoc());
        delegate.setDbCoupon(getDbCoupon());
        delegate.setCouponThemeCache(getCouponThemeCache());
        delegate.setOffLineFlag(getOffLineFlag());
        delegate.setVerifyUnionId(getVerifyUnionId());
        delegate.execute();
    }

    private void validate() {
        if (shouldSkipValidateUserInfo(getCouponThemeCache().getCouponGiveRule())) {
            getDbCoupon().setUserType(UserTypeEnum.C.getUserType());
            setOffLineFlag(Boolean.TRUE);
        } else {
            // 此券绑定的userId
            VerifyUserInfoDto verifyUserInfoDto = new VerifyUserInfoDto();
            verifyUserInfoDto.setVerifyPhone(getVerifyContext().getBindTel());
            verifyUserInfoDto.setDbUserId(getDbCoupon().getUserId());
            verifyUserInfoDto.setDbBindTel(getCouponEsDoc().getBindTel());
            verifyUserInfoDto.setUserType(getDbCoupon().getUserType());

            JSONArray applicableUserTypes = getCouponThemeApplicableUserTypes(getCouponThemeCache().getApplicableUserTypes());
            verifyUserInfoDto.setApplicableUserTypes(applicableUserTypes);

            verifyUserInfoDto.setVerifyFlag(CouponConstant.NO);
            // 校验--用户信息
            ResponseErrorCode errorCode = validateUserInfo4Single(verifyUserInfoDto);
            if (errorCode != null) {
                throw new BusinessException(errorCode);
            }

            setOffLineFlag(Boolean.FALSE);
            setVerifyUnionId(verifyUserInfoDto.getVerifyUnionId());
        }
    }
}
