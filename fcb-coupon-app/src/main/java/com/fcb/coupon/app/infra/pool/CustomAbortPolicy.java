package com.fcb.coupon.app.infra.pool;

import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class CustomAbortPolicy implements RejectedExecutionHandler {

    public CustomAbortPolicy() {
    }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        log.error("线程池繁忙，拒绝接收新任务", "Task " + r.toString() + " rejected from " + e.toString());
        throw new BusinessException(CommonErrorCode.OPERATE_FREQUENTLY);
    }
}