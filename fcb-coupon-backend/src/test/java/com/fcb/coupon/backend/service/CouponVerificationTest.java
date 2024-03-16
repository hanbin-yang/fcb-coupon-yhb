package com.fcb.coupon.backend.service;

import com.fcb.coupon.BaseTest;
import com.fcb.coupon.backend.model.dto.CouponVerificationStatisticDo;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-09-08 9:41
 */
public class CouponVerificationTest extends BaseTest {
    @Resource
    private CouponVerificationService couponVerificationService;
    @Test
    public void statisticVerificationCountTest() {
        ArrayList<Long> couponThemeIds = new ArrayList<>();
        couponThemeIds.add(111113333L);
        List<CouponVerificationStatisticDo> list = couponVerificationService.listVerificationCount(couponThemeIds);
        System.out.println("list = " + list);
    }
}
