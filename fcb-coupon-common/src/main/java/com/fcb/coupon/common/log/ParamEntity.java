package com.fcb.coupon.common.log;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 唐陆军
 * @Description 参数实体类
 * @createTime 2021年06月16日 11:40:00
 */
@Data
public class ParamEntity {

    private String methodStr;

    private long startTime;

    private long endTime;

    public String getMethodStr() {
        return this.methodStr;
    }

    public void setMethodStr(String methodStr) {
        this.methodStr = methodStr;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
