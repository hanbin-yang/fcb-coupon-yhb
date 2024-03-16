package com.fcb.coupon.app.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月17日 09:10:00
 */
@Data
@Component
@ConfigurationProperties(prefix = "growing.io")
public class GrowingIoProperties {

    /**
     * 运行模式，test:仅输出消息体，不发送消息，production: 发送消息
     */
    private String runMode;

    /**
     * GIO测试环境项目id
     */
    private String projectIdTest;

    /**
     * GIO测试环境项目数据源
     */
    private String projectDataSourceIdTest;

    /**
     * GIO生产环境项目id
     */
    private String projectIdProduction;

    /**
     * GIO生产环境项目数据源
     */
    private String projectDataSourceIdProduction;

    /**
     * 项目采集端地址
     */
    private String apiHost;

    /**
     * 消息发送间隔时间,单位ms（默认 100）
     */
    private String sendMsgInterval;

    /**
     * 消息发送线程数量 （默认 3）
     */
    private String sendMsgThread;

    /**
     * 消息队列大小 （默认 500）
     */
    private String msgStoreQueueSize;

    /**
     * 数据压缩 false:不压缩,true:压缩
     * 不压缩可节省cpu，压缩可省带宽
     */
    private String compress;

    /**
     * 日志级别输出 (debug | error)
     */
    private String loggerLevel;

    /**
     * 自定义日志输出实现类
     */
    private String loggerImplementation;

}
