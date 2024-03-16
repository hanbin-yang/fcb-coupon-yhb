package com.fcb.coupon.backend.controller;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.backend.infra.inteceptor.IgnoreAuthorityPath;
import com.fcb.coupon.backend.infra.inteceptor.IgnoreLogin;
import com.fcb.coupon.backend.uitls.I18nUtils;
import io.micrometer.core.instrument.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author YangHanBin
 * @date 2021-08-04 10:20
 */
@Controller
@RequestMapping
@Slf4j
@IgnoreAuthorityPath
public class I18nController {
    @RequestMapping(value = "/i18n.action", method = {RequestMethod.POST, RequestMethod.GET})
    public void execute(HttpServletRequest req, HttpServletResponse resp) {
        String poolNameInput = req.getParameter("poolName");
        String group = req.getParameter("group");
        String response = req.getParameter("response");
        if (StringUtils.isBlank(response)) {
            response = "script";
        }

        String consts = req.getParameter("const");
        String method = req.getParameter("method");
        if (StringUtils.isEmpty(group)) {
            group = "";
        }

        String poolNameUse = I18nUtils.POOL_NAME;
        if (StringUtils.isNotBlank(poolNameInput)) {
            poolNameUse = poolNameInput;
        }

        StringTokenizer tokenizer = new StringTokenizer(poolNameUse, ", ", false);
        ArrayList poolNames = new ArrayList();

        while(tokenizer.hasMoreTokens()) {
            poolNames.add(tokenizer.nextToken());
        }

        HashMap result;
        Iterator json;
        String json1;
        if ("flush_all".equals(method)) {
            result = new HashMap();
            json = poolNames.iterator();

            while(json.hasNext()) {
                json1 = (String)json.next();
                result.put(json1, "success");
            }

            renderJson(resp, result);
        } else {
            result = new HashMap();
            json = poolNames.iterator();

            while(json.hasNext()) {
                json1 = (String)json.next();
                Map map = I18nUtils.getI18nMap(I18nUtils.POOL_NAME);
                if (map != null) {
                    result.putAll(map);
                }
            }

            StringBuilder builder1;
            if (!"angular".equalsIgnoreCase(response) && !"script".equalsIgnoreCase(response)) {
                if ("vue".equalsIgnoreCase(response)) {
                    if (StringUtils.isEmpty(consts)) {
                        consts = "messages";
                    }

                    json1 = JSON.toJSONString(result);
                    builder1 = new StringBuilder();
                    builder1.append("var ");
                    builder1.append(consts);
                    builder1.append("={");
                    builder1.append("zh_CN");
                    builder1.append(":");
                    builder1.append(json1);
                    builder1.append("}");
                    renderScript(resp, builder1.toString());
                } else {
                    renderJson(resp, result);
                }
            } else {
                json1 = JSON.toJSONString(result);
                builder1 = new StringBuilder();
                builder1.append("String.prototype.format = function () {var result = this;if (arguments.length > 0) {for (var i = 0; i < arguments.length; i++) {if (arguments[i] != undefined) {var reg = new RegExp(\"({[\" + i + \"]})\", \"g\");result = result.replace(reg, arguments[i]);}}}return result;};function i18n() {var key = arguments[0];var args = [];if(arguments.length > 1){for(var i = 1;i< arguments.length;i++){args[i-1] = arguments[i];}}var value=_i18nMap_[key];if(value){return String.prototype.format.apply(value,args).toString();}return key.format(args).toString();};");
                builder1.append("var _i18nMap_=" + json1 + ";");
                renderScript(resp, builder1.toString());
            }
        }

    }


    private static void renderJson(HttpServletResponse response, Object object) {
        try {
            String e = "UTF-8";
            String fullContentType = "application/json;charset=" + e;
            response.setContentType(fullContentType);
            setHeader(response, false);
            PrintWriter writer = response.getWriter();
            String json = JSON.toJSONString(object);
            writer.write(json);
            writer.flush();
        } catch (IOException var6) {
            log.error(var6.getMessage(), var6);
        }

    }

    private static void renderScript(HttpServletResponse response, String script) {
        try {
            String e = "UTF-8";
            String fullContentType = "text/javascript;charset=" + e;
            response.setContentType(fullContentType);
            setHeader(response, false);
            PrintWriter writer = response.getWriter();
            writer.write(script);
            writer.flush();
        } catch (IOException var5) {
            log.error(var5.getMessage(), var5);
        }

    }

    private static void setHeader(HttpServletResponse response, boolean withCache) {
        if (!withCache) {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0L);
        }

    }
}
