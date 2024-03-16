package com.fcb.coupon.common.log;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author 唐陆军
 * @Description 入参打印
 * @createTime 2021年06月16日 11:44:00
 */
@Slf4j
public class RequestParamPrinter extends AbstractParamPrinter {

    private ParamEntity paramEntity;

    private ProceedingJoinPoint pjp;

    public RequestParamPrinter(ParamEntity paramEntity, ProceedingJoinPoint pjp) {
        this.paramEntity = paramEntity;
        this.pjp = pjp;
    }

    public void call() {
        Object[] args = this.pjp.getArgs();
        String argsStr = null;
        if (args != null) {
            argsStr = buildRequestParamStr(args);
        }
        log.info("{}\nrequest parameters：{}", this.paramEntity.getMethodStr(), argsStr);
    }

    private String buildRequestParamStr(Object[] args) {
        StringBuilder argsBuilder = new StringBuilder();
        for (Object arg : args) {
            try {
                Object param = processParam(arg);
                if (param instanceof String) {
                    argsBuilder.append(param.toString()).append(", ");
                } else {
                    argsBuilder.append(JSON.toJSONString(param)).append(", ");
                }
            } catch (Exception e) {
                argsBuilder.append(arg.toString()).append(", ");
            }
        }
        return argsBuilder.toString();
    }

}