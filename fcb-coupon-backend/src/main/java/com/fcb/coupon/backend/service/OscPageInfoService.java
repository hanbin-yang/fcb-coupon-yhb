package com.fcb.coupon.backend.service;

import com.alibaba.fastjson.JSONObject;

/**
 * @author YangHanBin
 * @date 2021-06-18 10:47
 */
public interface OscPageInfoService {
    String getValue(String key);

    JSONObject childJsonObject(String parentKey);

    JSONObject getJsonObject(String configKey);
}
