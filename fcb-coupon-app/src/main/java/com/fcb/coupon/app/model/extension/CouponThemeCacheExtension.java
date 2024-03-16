package com.fcb.coupon.app.model.extension;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.dto.CouponThemeCrowdScopeIdDto;
import com.fcb.coupon.common.enums.CouponDiscountType;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年09月03日 10:59:00
 */
public class CouponThemeCacheExtension {

    private CouponThemeCache couponThemeCache;

    public CouponThemeCacheExtension(CouponThemeCache couponThemeCache) {
        this.couponThemeCache = couponThemeCache;
    }

    /**
     * 获取使用规则
     */
    public String getUseRuleRemark() {
        String useRuleRemark = "";
        if (Objects.equals(CouponDiscountType.DISCOUNT.getType(), couponThemeCache.getCouponDiscountType())) {
            useRuleRemark = "最多减免" + new BigDecimal(couponThemeCache.getDiscountAmount() + "").stripTrailingZeros().toPlainString() + "元";
        } else {
            useRuleRemark = "满" + new BigDecimal(couponThemeCache.getUseLimit() + "").stripTrailingZeros().toPlainString() + "元可用";
        }

        if (couponThemeCache.getUseLimit().compareTo(new BigDecimal(0)) == 0) {
            useRuleRemark = "无门槛";
        }
        return useRuleRemark;
    }

    /**
     * 获取券值
     */
    public BigDecimal getCouponValue() {
        BigDecimal val;
        if (Objects.equals(CouponDiscountType.DISCOUNT.getType(), couponThemeCache.getCouponDiscountType())) {
            BigDecimal discount = new BigDecimal(couponThemeCache.getDiscountValue());
            // 折扣为2位小数，需要除以100
            BigDecimal divisor2 = new BigDecimal(100);
            val = discount.divide(divisor2, 2, BigDecimal.ROUND_HALF_UP);
        } else {
            val = couponThemeCache.getDiscountAmount();
        }
        return val;
    }


    /**
     * 适用人群json对象转数字数组
     */
    public List<Integer> getCrowdScopeIds() {
        if (StringUtils.isBlank(couponThemeCache.getApplicableUserTypes())) {
            return Collections.EMPTY_LIST;
        }

        CouponThemeCrowdScopeIdDto dto = JSON.parseObject(couponThemeCache.getApplicableUserTypes(), CouponThemeCrowdScopeIdDto.class);
        return dto.getIds();
    }

}
