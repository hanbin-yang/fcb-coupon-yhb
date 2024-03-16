package com.fcb.coupon.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月09日 19:52:00
 */
@Data
@Component
@ConfigurationProperties(prefix = "test")
public class TestConfiguration {


    private String name;

    private Integer age;
}
