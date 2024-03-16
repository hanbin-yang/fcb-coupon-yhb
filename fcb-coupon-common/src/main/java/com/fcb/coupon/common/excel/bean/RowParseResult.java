package com.fcb.coupon.common.excel.bean;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 14:44:00
 */
@Data
@Builder
public class RowParseResult {

    /**
     * 行号
     */
    private Integer rowNum;
    /**
     * 解析是否失败
     */
    private Boolean isFailure;

    /**
     * 引起原因
     */
    private String causeMessage;

    /**
     * 解析的数据
     */
    private Object rowBean;

}
