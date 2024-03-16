package com.fcb.coupon.backend.model.param.response;


import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import lombok.Data;

@Data
@XSheet(name = "导入券码结果", titleRowNum = 0)
public class CouponImportErrorResponse {

    @XCell(name = "序号")
    private Integer index;

    @XCell(name = "优惠券名称")
    private String couponName;

    @XCell(name = "操作类型")
    private String oprtName;

    @XCell(name = "券活动时期")
    private String startEndTime;

    @XCell(name = "第三方卡号/优惠券码")
    private String thirdCouponCode;



    @XCell(name = "导入状态")
    private String statusName;

    @XCell(name = "失败原因")
    private String reason;
}
