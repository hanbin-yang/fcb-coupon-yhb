package com.fcb.coupon.backend.model.param.response;

import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import lombok.Data;

import java.util.Date;

@Data
@XSheet(name = "第三方券导入结果")
public class CouponGenerateBatchExportResponse {


    @XCell(name = "序号")
    private Integer index;

    @XCell(name = "优惠券名称")
    private String themeTitle;

    @XCell(name = "操作类型")
    private String typeName;

    @XCell(name = "券活动时期")
    private String startEnd;

    @XCell(name = "发放状态")
    private String statusName;

    @XCell(name = "总张数")
    private Integer totalCount;

    @XCell(name = "成功张数")
    private Integer successCount;

    @XCell(name = "失败张数")
    private Integer errorCount;

    @XCell(name = "失败原因")
    private String errorReason;

    @XCell(name = "操作时间")
    private Date createTime;

    @XCell(name = "操作账号")
    private String createUsername;

    @XCell(name = "任务编号")
    private String generateId;


}
