package com.fcb.coupon.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.app.exception.CouponUserStatisticErrorCode;
import com.fcb.coupon.app.mapper.CouponUserStatisticMapper;
import com.fcb.coupon.app.model.entity.CouponUserStatisticEntity;
import com.fcb.coupon.app.service.CouponUserStatisticService;
import com.fcb.coupon.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月05日 19:32:00
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponUserStatisticServiceImpl extends ServiceImpl<CouponUserStatisticMapper, CouponUserStatisticEntity> implements CouponUserStatisticService {


    @Override
    @Transactional
    public void updateWithTx(CouponUserStatisticEntity entity) {
        int individualLimitResult = baseMapper.updateIndividualLimit(entity);

        if (individualLimitResult == 0) {
            log.error("updateUserStatisticLimitWithTx#updateIndividualLimit 超出个人总领券限制: dto={}", JSON.toJSONString(entity));
            throw new BusinessException(CouponUserStatisticErrorCode.OUT_OF_INDIVIDUAL_LIMIT);
        }

        int monthLimitResult = baseMapper.updateMonthLimit(entity);
        if (monthLimitResult == 0) {
            log.error("updateUserStatisticLimitWithTx#updateMonthLimit 超出个人每月领券限制: dto={}", JSON.toJSONString(entity));
            throw new BusinessException(CouponUserStatisticErrorCode.OUT_OF_MONTH_LIMIT);
        }

        int dayLimitResult = baseMapper.updateDayLimit(entity);
        if (dayLimitResult == 0) {
            log.error("updateUserStatisticLimitWithTx#updateDayLimit 超出个人每天领券限制: dto={}", JSON.toJSONString(entity));
            throw new BusinessException(CouponUserStatisticErrorCode.OUT_OF_DAY_LIMIT);
        }
    }
}
