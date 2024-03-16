package com.fcb.coupon.backend.common.kafka;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.BaseTest;
import org.junit.Test;
import org.springframework.kafka.core.KafkaTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @Author WeiHaiQi
 * @Date 2021-06-28 17:06
 **/
public class KafkaTemplateTest extends BaseTest {

    @Resource(name = "kafkaTemplate")
    private KafkaTemplate kafkaTemplate;

    @Test
    public void sendNoticTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("unionId", "unionId");
        map.put("initFlag", false);
        map.put("initiateTime", new Date());
        map.put("activityIds", "111111");
        kafkaTemplate.send("ODY_BAODI_ACTIVITY", JSON.toJSON(map).toString());
        System.out.println("");
    }
}
