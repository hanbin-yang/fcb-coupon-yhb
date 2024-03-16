package com.fcb.coupon.app.business.couponreceive.strategy;

import com.fcb.coupon.app.business.couponreceive.CouponReceiveContext;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponUserEntity;
import com.fcb.coupon.app.model.entity.CouponUserStatisticEntity;
import com.fcb.coupon.app.service.CouponService;
import com.fcb.coupon.common.enums.CouponTypeEnum;
import com.fcb.coupon.common.enums.YesNoEnum;
import com.fcb.coupon.common.util.AESPromotionUtil;
import com.fcb.coupon.common.util.CodeUtil;
import com.fcb.coupon.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * 自动券码领券
 * @author YangHanBin
 * @date 2021-08-16 14:02
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class AutoCouponCodeReceiveStrategy extends AbstractCouponReceiveStrategy {
    private final CouponService couponService;
    @Override
    public boolean supports(Integer couponType) {
        return CouponTypeEnum.COUPON_TYPE_VIRTUAL.getType().equals(couponType);
    }

    @Override
    public CouponEntity receive(CouponReceiveContext context) {
        Tuple3<CouponEntity, CouponUserEntity, CouponUserStatisticEntity> tuple3 = prepareCouponReceiveRelatedEntityBean(context, RedisUtil.generateId());

        doReceive(tuple3.getT1(), tuple3.getT2(), tuple3.getT3());
        context.setCouponEntity(tuple3.getT1());

        return tuple3.getT1();
    }

    private void doReceive(CouponEntity couponEntity, CouponUserEntity couponUserEntity, CouponUserStatisticEntity couponUserStatisticEntity) {
        couponService.receiveCouponWithTx(couponEntity, couponUserEntity, couponUserStatisticEntity);
    }

    private Tuple3<CouponEntity, CouponUserEntity, CouponUserStatisticEntity> prepareCouponReceiveRelatedEntityBean(CouponReceiveContext context, Long couponId) {
        CouponThemeCache couponThemeCache = context.getCouponThemeCache();

        CouponEntity couponEntity = getCouponEntity(context, couponId, couponThemeCache);
        couponEntity.setCouponCode(AESPromotionUtil.encrypt(CodeUtil.generateCouponCode()));
        couponEntity.setThemeTitle(couponThemeCache.getThemeTitle());
        couponEntity.setCouponType(couponThemeCache.getCouponType());
        couponEntity.setIsDeleted(YesNoEnum.NO.getValue());
        couponEntity.setCreateTime(context.getReceiveTime());

        CouponUserEntity couponUserEntity = prepareCouponUserEntity(context, couponEntity);
        CouponUserStatisticEntity couponUserStatisticEntity = prepareCouponUserStatisticBean(context);
        return Tuples.of(couponEntity, couponUserEntity, couponUserStatisticEntity);
    }
}
