package com.fcb.coupon.backend.business.couponSend.strategy;

import com.fcb.coupon.backend.business.couponSend.CouponSendStrategy;
import com.fcb.coupon.backend.model.dto.CouponMergedDto;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponSendLogEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;
import com.fcb.coupon.common.enums.CouponDiscountType;
import com.fcb.coupon.common.enums.CouponEffDateCalType;
import com.fcb.coupon.common.enums.CouponStatusEnum;
import com.fcb.coupon.common.enums.YesNoEnum;
import com.fcb.coupon.common.util.AESPromotionUtil;
import com.fcb.coupon.common.util.CodeUtil;
import com.fcb.coupon.common.util.DateUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月06日 10:13:00
 */
public abstract class AbstractCouponSendStrategy implements CouponSendStrategy {


    protected void setFinishSendContexts(List<CouponSendContext> couponSendContexts, List<CouponMergedDto> couponMergedDtos) {
        for (int i = 0; i < couponSendContexts.size(); i++) {
            CouponSendContext couponSendContext = couponSendContexts.get(i);
            CouponMergedDto couponMergedDto = couponMergedDtos.get(i);
            couponSendContext.success(couponMergedDto.getCouponEntity(), couponMergedDto.getCouponUserEntity());
        }
    }


    protected CouponMergedDto generateMergedCoupon(CouponThemeEntity couponTheme, CouponSendContext sendContext, Long couponId) {
        CouponMergedDto couponMergedDto = new CouponMergedDto();
        //优惠券信息
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setId(couponId);
        couponEntity.setCouponCode(AESPromotionUtil.encrypt(CodeUtil.generateCouponCode()));
        couponEntity.setSource(sendContext.getSource());
        couponEntity.setSourceId(sendContext.getSourceId());
        couponEntity.setCouponThemeId(couponTheme.getId());
        couponEntity.setThemeTitle(couponTheme.getThemeTitle());
        couponEntity.setCouponType(couponTheme.getCouponType());
        couponEntity.setStatus(CouponStatusEnum.STATUS_USE.getStatus());
        //固定天数
        if (CouponEffDateCalType.DAYS.getType().equals(couponTheme.getEffDateCalcMethod())) {
            couponEntity.setStartTime(new Date());
            couponEntity.setEndTime(DateUtils.getDelayTime(couponEntity.getStartTime(), couponTheme.getEffDateDays()));
        } else {
            couponEntity.setStartTime(couponTheme.getStartTime());
            couponEntity.setEndTime(couponTheme.getEndTime());
        }
        couponEntity.setUserType(sendContext.getUserType());
        couponEntity.setUserId(sendContext.getUserId());
        couponEntity.setIsDeleted(YesNoEnum.NO.getValue());
        couponEntity.setCreateTime(new Date());
        couponEntity.setCreateUserid(sendContext.getCreateUserid());
        couponEntity.setCreateUsername(sendContext.getCreateUsername());
        couponMergedDto.setCouponEntity(couponEntity);

        couponEntity.setCouponDiscountType(couponTheme.getCouponDiscountType());
        if (CouponDiscountType.DISCOUNT.getType().equals(couponTheme.getCouponDiscountType())) {
            couponEntity.setCouponValue(new BigDecimal(couponTheme.getDiscountValue()));
        } else {
            couponEntity.setCouponValue(couponTheme.getDiscountAmount());
        }

        //优惠券用户领券信息
        CouponUserEntity couponUserEntity = new CouponUserEntity();
        couponUserEntity.setCouponId(couponEntity.getId());
        couponUserEntity.setCouponThemeId(couponEntity.getCouponThemeId());
        couponUserEntity.setStatus(CouponStatusEnum.STATUS_USE.getStatus());
        couponUserEntity.setUserType(sendContext.getUserType());
        couponUserEntity.setUserId(couponEntity.getUserId());
        couponUserEntity.setBindTel(sendContext.getBindTel());
        couponUserEntity.setCreateTime(new Date());
        couponUserEntity.setEndTime(couponEntity.getEndTime());
        couponMergedDto.setCouponUserEntity(couponUserEntity);

        //存在事务id，需要防重，防重信息
        if (!StringUtils.isEmpty(sendContext.getTransactionId())) {
            CouponSendLogEntity couponSendLogEntity = new CouponSendLogEntity();
            couponSendLogEntity.setCouponThemeId(couponEntity.getCouponThemeId());
            couponSendLogEntity.setTransactionId(sendContext.getTransactionId());
            couponSendLogEntity.setCreateTime(new Date());
            couponSendLogEntity.setCreateUserid(sendContext.getCreateUserid());
            couponSendLogEntity.setCreateUsername(sendContext.getCreateUsername());
            couponMergedDto.setCouponSendLogEntity(couponSendLogEntity);
        }

        return couponMergedDto;
    }

}
