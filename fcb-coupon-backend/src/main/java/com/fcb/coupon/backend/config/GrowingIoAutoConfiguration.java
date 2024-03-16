package com.fcb.coupon.backend.config;

import com.fcb.coupon.backend.properties.GrowingIoProperties;
import io.growing.sdk.java.GrowingAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Properties;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月17日 09:09:00
 */
@Configuration
public class GrowingIoAutoConfiguration {

    @Bean
    public GrowingAPI growingAPI(GrowingIoProperties growingIoProperties) {
        Properties properties = new Properties();
        properties.setProperty("run.mode", growingIoProperties.getRunMode());
        properties.setProperty("project.id.test", growingIoProperties.getProjectIdTest());
        properties.setProperty("project.dataSourceId.test", growingIoProperties.getProjectDataSourceIdTest());
        properties.setProperty("project.id.production", growingIoProperties.getProjectIdProduction());
        properties.setProperty("project.dataSourceId.production", growingIoProperties.getProjectDataSourceIdProduction());

        properties.setProperty("api.host", growingIoProperties.getApiHost());
        properties.setProperty("send.msg.interval", growingIoProperties.getSendMsgInterval());
        properties.setProperty("send.msg.thread", growingIoProperties.getSendMsgThread());
        properties.setProperty("msg.store.queue.size", growingIoProperties.getMsgStoreQueueSize());
        properties.setProperty("compress", growingIoProperties.getCompress());
        properties.setProperty("logger.level", growingIoProperties.getLoggerLevel());
        properties.setProperty("logger.implementation", growingIoProperties.getLoggerImplementation());
        GrowingAPI.initConfig(properties);

        String projectKey = null;
        String dataSourceId = null;
        if ("test".equals(growingIoProperties.getRunMode())) {
            projectKey = growingIoProperties.getProjectIdTest();
            dataSourceId = growingIoProperties.getProjectDataSourceIdTest();
        } else {
            projectKey = growingIoProperties.getProjectIdProduction();
            dataSourceId = growingIoProperties.getProjectDataSourceIdProduction();
        }

        return new GrowingAPI.Builder().setProjectKey(projectKey).setDataSourceId(dataSourceId).build();
    }


}
