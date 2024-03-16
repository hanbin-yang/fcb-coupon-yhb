package com.fcb.coupon.backend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.fcb.coupon.backend.service.OscPageInfoService;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import com.fcb.coupon.common.midplatform.MidPlatformLoginHelper;
import com.fcb.coupon.common.util.SpringBeanFactory;
import com.google.common.base.CaseFormat;
import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author YangHanBin
 * @date 2021-06-18 10:47
 */
@Service
@RefreshScope
public class OscPageInfoServiceImpl implements OscPageInfoService {
    @Autowired
    private Environment env;

    @Override
    public String getValue(String key) {
        if (StringUtils.isNotBlank(key)) {
            String realKey = "promotion." + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, key);
            return env.getProperty(realKey);
        }

        return null;
    }

    @Override
    public JSONObject childJsonObject(String parentKey) {
        Object bean = SpringBeanFactory.getBean(parentKey);
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
        Set<String> excludes = filter.getExcludes();
        List<String> excludeFields = new ArrayList<>();
        excludeFields.add("CGLIB$BOUND");
        excludeFields.add("CGLIB$CALLBACK_0");
        excludeFields.add("CGLIB$CALLBACK_1");
        excludeFields.add("CGLIB$CALLBACK_2");
        excludeFields.add("$$beanFactory");
        excludes.addAll(excludeFields);

        String string = JSON.toJSONString(bean, filter);
        JSONObject result = new JSONObject();
        JSONObject o = JSON.parseObject(string);
        o.forEach((k, v) -> result.put(k, convertObj((String)v)));
        //在pageConfig里面增加platformId，为了区分运营和商家平台
        result.put("platformId", 8);

        return result;
    }

    @Override
    public JSONObject getJsonObject(String configKey) {
        String result = getValue(configKey);
        if (result != null) {
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("result", convertObj(result));
            return jsonResult;
        }

        return null;
    }

    private Object convertObj(String value) {
        try {
            if (Objects.equal(StringUtils.EMPTY, value)) {
                return value;
            }
            Map<String, Object> map = JSONObject.parseObject(value, Map.class);
            JSONObject internalObj = new JSONObject();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String v = entry.getValue().toString();
                internalObj.put(entry.getKey(), JSONArray.parseArray(v));
            }
            return internalObj;
        } catch (Exception e) {
            try {
                return JSONArray.parseArray(value);
            } catch (Exception e2) {
                return value;
            }
        }
    }
}
