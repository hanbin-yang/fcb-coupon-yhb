package com.fcb.coupon.common.util;

import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;
import java.util.HashMap;

public class LuaUtil {
    private static final HashMap<String, String> luaMap= new HashMap<>();

    private static final  HashMap<String, String> shaMap = new HashMap<>();

    static {
        RedissonClient redissonClient = SpringBeanFactory.getBean(RedissonClient.class);
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:script/*.lua");
            for (Resource resource : resources) {
                ResourceScriptSource scriptSource = new ResourceScriptSource(resource);
                String scriptStr = scriptSource.getScriptAsString();
                luaMap.put(resource.getFilename(), scriptStr);
            }
            luaMap.forEach((k, v) -> {
                String sha = redissonClient.getScript(StringCodec.INSTANCE).scriptLoad(v);
                shaMap.put(k, sha);
            });
        } catch (IOException e) {
            throw new RuntimeException("LuaUtil init error!", e);
        }

    }

    public static String getSha(String fileName) {
        return shaMap.get(fileName);
    }

    public static String getLua(String fileName) {
        return luaMap.get(fileName);
    }
}