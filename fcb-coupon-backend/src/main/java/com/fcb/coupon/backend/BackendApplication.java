package com.fcb.coupon.backend;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;


/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月09日 19:42:00
 */

@Slf4j
@MapperScan(basePackages = {"com.fcb.coupon.backend.mapper"})
@SpringBootApplication(scanBasePackages = {"com.fcb.coupon.backend", "com.fcb.coupon.common"})
@EnableElasticsearchRepositories(basePackages = "com.fcb.coupon.backend.elasticsearch.repository")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.fcb.coupon.backend.remote")
public class BackendApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        SpringApplication.run(BackendApplication.class, args);

        System.out.println(AnsiOutput.toString(AnsiColor.RED, "application start successfully !!!"));
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        return builder.sources(BackendApplication.class);
    }
}
