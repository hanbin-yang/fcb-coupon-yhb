package com.fcb.coupon.backend.business.couponSend.handler;

import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.model.bo.CouponBatchSendBo;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.enums.YesNoEnum;
import com.fcb.coupon.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @author 唐陆军
 * @Description saas发券
 * @createTime 2021年08月24日 19:06:00
 */
@Slf4j
@Component
public class CouponSendSaasHandler extends AbstractCouponSendHandler {

    @Override
    public Boolean supports(Integer sendUserType) {
        return UserTypeEnum.SAAS.getUserType().equals(sendUserType);
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
        //目前saas对用户不做任何处理
    }

}