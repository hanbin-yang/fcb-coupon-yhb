package com.fcb.coupon.backend.model.param.request;

import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import lombok.Data;

/**
 * @author HanBin_Yang
 * @since 2021/6/22 15:02
 */
@Data
@XSheet(name = "批量导入商家店铺", titleRowNum = 0)
public class AddMerchantImportRequest {
    @XCell(name = "*商家编码")
    private String orgCode;

    @XCell(name = "*商家名称")
    private String orgName;
}
