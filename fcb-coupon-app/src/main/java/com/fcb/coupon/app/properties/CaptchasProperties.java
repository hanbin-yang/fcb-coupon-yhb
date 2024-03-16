package com.fcb.coupon.app.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 马仕琼
 * @Description
 * @createTime 2021年08月19日 20:04:00
 */
@Data
@Component
@ConfigurationProperties(prefix = "captchas")
public class CaptchasProperties {

    /**
     * 验证码校验通过后，有效时间（小时）
     */
    private Integer validHourTime;

    /**
     * 连续点获取间隔（秒）
     */
    private Integer continuitySendTime;

    /**
     * 点获取验证码后，等待进行校验的有效时长（分钟）
     */
    private Integer expireMinTime;

    /**
     * 获取验证码，最大可失败次数
     */
    private Integer maxMobileCaptchasFailCount;

    /**
     * 唯一的业务ID
     */
    private String voiceRefId;

    /**
     * 短信节点代码C端
     */
    private String nodeCodeC;

    /**
     * 短信节点代码B端
     */
    private String nodeCodeB;


}
