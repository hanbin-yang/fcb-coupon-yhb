package com.fcb.coupon.backend.model.param.request;

import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import lombok.Data;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 21:35
 */
@Data
@XSheet(name = "批量导入添加店铺", titleRowNum = 0)
public class AddStoreImportRequest {
    @XCell(name = "*店铺编码")
    private String orgCode;

    @XCell(name = "*店铺名称")
    private String orgName;
}
