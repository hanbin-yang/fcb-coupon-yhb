package com.fcb.coupon.common.log;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月16日 14:03:00
 */
@Slf4j
public class ResponseParamPrinter extends AbstractParamPrinter {

    private ParamEntity paramEntity;

    private Object returnValue;


    public ResponseParamPrinter(ParamEntity paramEntity, Object returnValue) {
        this.paramEntity = paramEntity;
        this.returnValue = returnValue;
    }

    public void call() {
        StringBuilder returnBuilder = new StringBuilder();
        try {
            Object param = processParam(this.returnValue);
            if (param instanceof String) {
                returnBuilder.append(param.toString());
            } else {
                returnBuilder.append(JSON.toJSONString(param));
            }
        } catch (Exception e) {
            returnBuilder.append(this.returnValue.toString());
        }
        long cost = this.paramEntity.getEndTime() - this.paramEntity.getStartTime();

        log.info("{}\nresponse parameters({}ms)：{}", new Object[]{this.paramEntity.getMethodStr(), Long.valueOf(cost), returnBuilder.toString()});
    }
}