package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.common.excel.bean.RowParseResult;
import lombok.Data;

import java.util.Map;

/**
 * @author HanBin_Yang
 * @since 2021/6/24 14:14
 */
@Data
public class BatchVerifyBo {
    private Long userId;
    private String username;

    /**
     * 异步任务主键id
     */
    private Long asyncTaskId;

    /**
     * 导入数据map
     */
    Map<Integer, RowParseResult> importDataMap;

}
