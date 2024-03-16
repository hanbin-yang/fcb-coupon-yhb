package com.fcb.coupon.app.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月26日 10:20:00
 */
@Data
@Component
@ConfigurationProperties(prefix = "coupon.receive")
public class CouponReceiveProperties {

    private Integer videoLimitRate = 60;

    private Integer videoLimitInterval = 1;
}
