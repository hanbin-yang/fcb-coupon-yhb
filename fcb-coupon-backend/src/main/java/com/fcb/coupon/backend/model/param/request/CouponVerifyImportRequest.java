package com.fcb.coupon.backend.model.param.request;

import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import lombok.Data;

/**
 * @author HanBin_Yang
 * @since 2021/6/24 13:48
 */
@Data
@XSheet(name = "批量导入核销模板", titleRowNum = 0)
public class CouponVerifyImportRequest {
    @XCell(name = "*券码")
    private String couponCode;

    @XCell(name = "*认购书编号")
    private String subscribeCode;

    @XCell(name = "*手机号/账号")
    private String verifyPhone;

    @XCell(name = "*楼盘编码")
    private String buildCode;
}
