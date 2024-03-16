package com.fcb.coupon.backend.model.dto;

import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import lombok.Data;

/**
 * @author HanBin_Yang
 * @since 2021/6/24 14:56
 */
@Data
@XSheet(name = "批量导入核销结果", titleRowNum = 0)
public class CouponImportVerifyResultDto {
    @XCell(name = "序号")
    private Integer rowNum;

    @XCell(name = "优惠券码")
    private String couponCode;

    @XCell(name = "操作类型")
    private String operationType;

    @XCell(name = "认购书编号")
    private String subscribeCode;

    @XCell(name = "核销状态")
    private String status;

    @XCell(name = "操作时间")
    private String operateTime;

    @XCell(name = "操作账号")
    private String oprUserName;

    @XCell(name = "任务编号")
    private Long taskId;

    @XCell(name = "备注")
    private String remark;
}
