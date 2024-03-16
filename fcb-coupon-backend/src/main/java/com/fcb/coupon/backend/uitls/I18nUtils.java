package com.fcb.coupon.backend.uitls;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class I18nUtils {
    private static final Logger logger = LoggerFactory.getLogger(I18nUtils.class);
    
    private I18nUtils() {}
    
    public static final String POOL_NAME = "promotion";
    public static final Pattern pattern = Pattern.compile("\\#(.*?)\\#");
    
    private static final String LINE_STR = "\n";
    private static final String QUOTE_STR = "&##&";
    private static Map<String, Map<String, String>> cache = Maps.newConcurrentMap();
    
    /**
     * 获取描述（中英双语）
     * @param str1 第一语言
     * @param str2 第二语言
     * @return
     */
    public static String getBilingualismDesc(String str1, String str2){
        if(str1 == null){
            str1 = "";
        }
        if(str2 == null){
            str2 = "";
        }
        return str1;
    }
    
    public static <T> T convertI18n(Object value, Class<T> t){
        if(value==null) {
            return null;
        }
        try{
            T newValue=null;
            String valueString= JSON.toJSONString(value);
            
            Matcher m = pattern.matcher(valueString);
            while(m.find()){
                String k=m.group(1);
                if(StringUtils.isEmpty(k)) {
                    continue;
                }
                String v = getI18n(k);
                if(StringUtils.isEmpty(v)) {
                    continue;
                }
                valueString=valueString.replaceAll("#"+k+"#",v);
            }
            newValue= JSON.parseObject(valueString,t);
            return newValue;
        }catch(Exception e){
            logger.error("I18nUtils.convertI18n",e);
            return null;
        }
    }
    
    public static String getI18n(String key) {
        return getI18n(POOL_NAME, key);
    }
    
    public static String getI18n(String poolName, String key) {
        return getI18n(poolName, key, getLocale(), key);
    }
    
    public static String getI18nWithDefault(String key, String defaultValue) {
        return getI18n(POOL_NAME, key, getLocale(), defaultValue);
    }
    
    public static String getI18n(String poolName, String key, String locale, String defaultValue) {
        Map<String, String> map = getI18nMap(poolName, locale);
        if (map != null && map.get(key) != null) {
            return map.get(key);
        }
        return defaultValue;
    }
    
    public static Map<String, String> getI18nMap(String poolName) {
        return getI18nMap(poolName, getLocale());
    }
    
    public static Map<String, String> getI18nMap(String poolName, String locale) {
        return doReadFile(poolName, locale);
    }
    
    private static Map<String, String> doReadFile(String poolName, String locale) {
        String cacheKey = poolName + "&" + locale;
        Map<String, String> map = cache.get(cacheKey);
        if (map == null) {
            map = Maps.newHashMap();
            cache.put(cacheKey, map);
            String filePath = "mtc/" + poolName + "_" + locale + ".txt";
            try (InputStream is = I18nUtils.class.getClassLoader().getResourceAsStream(filePath)) {
                List<String> lines = IOUtils.readLines(is, Charset.forName("utf-8"));
                
                StringBuilder key = new StringBuilder(), value = new StringBuilder();
                
                final int quoteLen = QUOTE_STR.length();
                boolean isKeyStart = false, isKeyEnd = false, isValueStart = false, isValueEnd = false;
                
                for (String line : lines) {
                    int kvIdx = line.indexOf(QUOTE_STR + "," + QUOTE_STR);
                    
                    if (line.startsWith(QUOTE_STR)) {
                        if (!isKeyStart) {
                            isKeyStart = true;
                        }
                        line = line.substring(quoteLen);
                        kvIdx -= quoteLen;
                    }
                    
                    boolean isKeyStartClone = isKeyStart;
                    if (kvIdx != -1) {
                        isKeyStartClone = false;
                        isKeyEnd = true;
                        isValueStart = true;
                        isValueEnd = false;
                        
                        if (line.endsWith(QUOTE_STR)) {
                            isValueEnd = true;
                            line = line.substring(0, line.length() - quoteLen);
                        }
                    }
                    
                    if (isKeyStart) {
                        appendLineSeperator(key);
                        key.append(kvIdx != -1 ? line.substring(0, kvIdx) : line);
                    }
                    
                    isKeyStart = isKeyStartClone;
                    
                    if (isValueStart) {
                        appendLineSeperator(value);
                        value.append(line.substring(kvIdx != -1 ? kvIdx + quoteLen * 2 + 1 : 0));
                    }
                    
                    if (isKeyEnd && isValueEnd) {
                        if (key.toString().trim().length() > 0) {
                            map.put(key.toString(), value.toString());
                        }
                        key = new StringBuilder();
                        value = new StringBuilder();
                        isKeyStart = isKeyEnd = isValueStart = isValueEnd = false;
                    }
                }
                
                return map;
            } catch (Exception e) {
                logger.error("An exception occured on read pool file: " + filePath, e);
                return Collections.emptyMap();
            }
        }
        return map;
    }
    
    private static void appendLineSeperator(StringBuilder buff) {
        if (buff.length() > 1) {
            buff.append(LINE_STR);
        }
    }
    
    private static String getLocale() {
        return "zh_CN";
    }
    
    public static void main(String[] args) {
        System.out.println(doReadFile("promotion", getLocale()));
    }
}
