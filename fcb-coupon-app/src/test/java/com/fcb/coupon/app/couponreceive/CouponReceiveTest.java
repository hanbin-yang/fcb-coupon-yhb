package com.fcb.coupon.app.couponreceive;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.BaseTest;
import com.fcb.coupon.app.business.couponreceive.CouponReceiveBusiness;
import com.fcb.coupon.app.model.bo.CouponReceiveBo;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.common.enums.CouponSourceTypeEnum;
import com.fcb.coupon.common.enums.UserTypeEnum;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author YangHanBin
 * @date 2021-08-26 14:02
 */
public class CouponReceiveTest extends BaseTest {
    @Autowired
    private CouponReceiveBusiness couponReceiveBusiness;

    @Test
    public void receiveTest() {
        CouponReceiveBo bo = new CouponReceiveBo();
        bo.setCouponThemeId(2108050000000019L);
        bo.setUserType(UserTypeEnum.B.getUserType());
        bo.setUserMobile("13590042801");
        bo.setSource(CouponSourceTypeEnum.COUPON_SOURCE_MARKETING_ACTIVITY.getSource());
        bo.setSourceId("11111222");
        bo.setReceiveCount(1);

        CouponThemeCache couponThemeCache = couponReceiveBusiness.getCouponThemeCache(bo.getCouponThemeId());
        CouponEntity couponEntity = couponReceiveBusiness.receive(bo, couponThemeCache);
        System.out.println("领券成功: couponEntity = " + JSON.toJSONString(couponEntity));
    }

}
