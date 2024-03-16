package com.fcb.coupon.app.business.couponreceive.strategy;

import com.fcb.coupon.app.business.couponreceive.CouponReceiveContext;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponUserEntity;
import com.fcb.coupon.app.model.entity.CouponUserStatisticEntity;
import com.fcb.coupon.common.enums.CouponDiscountType;
import com.fcb.coupon.common.enums.CouponEffDateCalType;
import com.fcb.coupon.common.enums.CouponStatusEnum;
import com.fcb.coupon.common.util.DateUtils;

import java.math.BigDecimal;

/**
 * @author YangHanBin
 * @date 2021-08-16 13:57
 */
public abstract class AbstractCouponReceiveStrategy implements CouponReceiveStrategy {
    protected CouponUserEntity prepareCouponUserEntity(CouponReceiveContext context, CouponEntity couponEntity) {
        CouponUserEntity couponUserEntity = new CouponUserEntity();
        couponUserEntity.setCouponId(couponEntity.getId());
        couponUserEntity.setCouponThemeId(context.getCouponThemeId());
        couponUserEntity.setStatus(CouponStatusEnum.STATUS_USE.getStatus());
        couponUserEntity.setUserType(context.getUserType());
        couponUserEntity.setUserId(context.getUserId());
        couponUserEntity.setBindTel(context.getUserMobile());
        couponUserEntity.setEndTime(couponEntity.getEndTime());
        couponUserEntity.setCreateTime(context.getReceiveTime());
        return couponUserEntity;
    }

    protected CouponUserStatisticEntity prepareCouponUserStatisticBean(CouponReceiveContext context) {
        CouponThemeCache couponThemeCache = context.getCouponThemeCache();
        Integer individualLimit = couponThemeCache.getIndividualLimit();
        Integer everyMonthLimit = couponThemeCache.getEveryMonthLimit();
        Integer everyDayLimit = couponThemeCache.getEveryDayLimit();

        CouponUserStatisticEntity couponUserStatisticEntity = new CouponUserStatisticEntity();
        couponUserStatisticEntity
                .setCouponThemeId(context.getCouponThemeId())
                .setUserId(context.getUserId())
                .setUserType(context.getUserType())
                .setLastReceiveDate(context.getReceiveTime())
                .setReceiveCount(context.getReceiveCount())
                .setIndividualLimit(individualLimit == null || individualLimit == 0 ? Integer.MAX_VALUE : individualLimit)
                .setMonthLimit(everyMonthLimit == null || everyMonthLimit == 0 ? Integer.MAX_VALUE : everyMonthLimit)
                .setDayLimit(everyDayLimit == null || everyDayLimit == 0 ? Integer.MAX_VALUE : everyDayLimit)

        ;
        return couponUserStatisticEntity;
    }

    protected CouponEntity getCouponEntity(CouponReceiveContext context, Long couponId, CouponThemeCache couponThemeCache) {
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setId(couponId);

        couponEntity.setCouponThemeId(context.getCouponThemeId());
        couponEntity.setUserId(context.getUserId());
        couponEntity.setUserType(context.getUserType());
        couponEntity.setSource(context.getSource());
        couponEntity.setSourceId(context.getSourceId());
        couponEntity.setStatus(CouponStatusEnum.STATUS_USE.getStatus());
        couponEntity.setCouponDiscountType(couponThemeCache.getCouponDiscountType());
        if (CouponDiscountType.DISCOUNT.getType().equals(couponThemeCache.getCouponDiscountType())) {
            Integer discountValue = couponThemeCache.getDiscountValue();
            couponEntity.setCouponValue(new BigDecimal(discountValue));
        } else {
            couponEntity.setCouponValue(couponThemeCache.getDiscountAmount());
        }
        if (CouponEffDateCalType.DAYS.getType().equals(couponThemeCache.getEffDateCalcMethod())) {
            // 自领取几天后过期
            couponEntity.setStartTime(context.getReceiveTime());
            couponEntity.setEndTime(DateUtils.getDelayTime(couponEntity.getStartTime(), couponThemeCache.getEffDateDays()));
        } else {
            // 固定有效期
            couponEntity.setStartTime(couponThemeCache.getEffDateStartTime());
            couponEntity.setEndTime(couponThemeCache.getEffDateEndTime());
        }
        return couponEntity;
    }
}
