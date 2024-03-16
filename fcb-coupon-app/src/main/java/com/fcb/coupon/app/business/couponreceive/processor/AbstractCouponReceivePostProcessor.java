package com.fcb.coupon.app.business.couponreceive.processor;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.app.business.couponreceive.CouponReceiveContext;
import com.fcb.coupon.app.exception.CouponReceiveErrorCode;
import com.fcb.coupon.app.exception.CouponUserStatisticErrorCode;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.dto.PersonalReceiveDo;
import com.fcb.coupon.app.model.dto.UserReceiveMemento;
import com.fcb.coupon.app.mq.producer.CouponEsSyncProducer;
import com.fcb.coupon.app.service.CouponUserStatisticCacheService;
import com.fcb.coupon.common.constant.PersonalReceiveOverrun;
import com.fcb.coupon.common.exception.BusinessException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author YangHanBin
 * @date 2021-08-16 11:42
 */
@Slf4j
public abstract class AbstractCouponReceivePostProcessor implements CouponReceivePostProcessor {
    @Resource
    private CouponEsSyncProducer couponEsSyncProducer;
    @Resource
    private CouponUserStatisticCacheService couponUserStatisticCacheService;

    @Override
    public void postProcessBeforeReceive(CouponReceiveContext context) {
        // redis预扣减个人领券限制
        deductRedisCouponUserStatistic(context);
    }

    @Override
    public void postProcessAfterReceive(CouponReceiveContext context) {
        // 更新es
        couponEsSyncProducer.sendSyncEsMessage(Lists.newArrayList(context.getCouponEntity().getId()));
    }

    private void deductRedisCouponUserStatistic(CouponReceiveContext context) {
        CouponThemeCache couponThemeCache = context.getCouponThemeCache();
        PersonalReceiveDo dto = new PersonalReceiveDo();
        dto
                .setCouponThemeId(context.getCouponThemeId())
                .setUserId(context.getUserId())
                .setUserType(context.getUserType())
                .setReceiveCount(context.getReceiveCount())
                .setTotalLimit(couponThemeCache.getIndividualLimit())
                .setMonthLimit(couponThemeCache.getEveryMonthLimit())
                .setDayLimit(couponThemeCache.getEveryDayLimit())
                .setReceiveTime(context.getReceiveTime())
        ;

        // redis扣减
        UserReceiveMemento memento = couponUserStatisticCacheService.deductStock(dto);
        if (Objects.isNull(memento)) {
            throw new BusinessException(CouponReceiveErrorCode.COUPON_THEME_NOT_EXIST);
        }
        if (Objects.equals(memento.getRealReceiveCount(), PersonalReceiveOverrun.OUT_TOTAL_LIMIT)) {
            log.error("领券失败 redis判断 超出个人总领券限制: dto={}", JSON.toJSONString(dto));
            throw new BusinessException(CouponUserStatisticErrorCode.OUT_OF_INDIVIDUAL_LIMIT);
        } else if (Objects.equals(memento.getRealReceiveCount(), PersonalReceiveOverrun.OUT_MONTH_LIMIT)) {
            log.error("领券失败 redis判断 超出本月领券限制: dto={}", JSON.toJSONString(dto));
            throw new BusinessException(CouponUserStatisticErrorCode.OUT_OF_MONTH_LIMIT);
        } else if (Objects.equals(memento.getRealReceiveCount(), PersonalReceiveOverrun.OUT_DAY_LIMIT)) {
            log.error("领券失败 redis判断 超出当天领券限制: dto={}", JSON.toJSONString(dto));
            throw new BusinessException(CouponUserStatisticErrorCode.OUT_OF_DAY_LIMIT);
        }
        // 设置备忘录
        context.setUserReceiveMemento(memento);
    }
}
