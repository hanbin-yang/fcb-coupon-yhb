package com.fcb.coupon.app;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月09日 19:42:00
 */

@Slf4j
@MapperScan(basePackages = {"com.fcb.coupon.app.mapper"})
@SpringBootApplication(scanBasePackages = {"com.fcb.coupon.app", "com.fcb.coupon.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.fcb.coupon.app.remote")
public class AppApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
        System.out.println(AnsiOutput.toString(AnsiColor.RED, "application start successfully !!!"));
    }
}
