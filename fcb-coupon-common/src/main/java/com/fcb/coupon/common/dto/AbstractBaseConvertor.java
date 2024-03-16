package com.fcb.coupon.common.dto;

/**
 * input 转换 bo
 * @author YangHanBin
 * @date 2021-06-11 10:14
 */
public abstract class AbstractBaseConvertor<R> {
    public abstract R convert();
}