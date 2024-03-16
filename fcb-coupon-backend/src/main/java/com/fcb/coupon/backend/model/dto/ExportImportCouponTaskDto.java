package com.fcb.coupon.backend.model.dto;

import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import lombok.Data;

import java.io.Serializable;

/**
 * 批量发券任务导出DTO
 *
 * @Author WeiHaiQi
 * @Date 2021-08-03 11:35
 **/
@Data
@XSheet(name = "券明细列表", titleRowNum = 0)
public class ExportImportCouponTaskDto implements Serializable {

    @XCell(name = "序号")
    private String snum;

    @XCell(name = "优惠券名称")
    private String themeTitle;

    @XCell(name = "操作类型")
    private String type;

    @XCell(name = "券活动时期")
    private String themeTime;

    @XCell(name = "发放状态")
    private String sendCouponStatus;

    @XCell(name = "张数")
    private Integer generateNums;

    @XCell(name = "操作时间")
    private String createTime;

    @XCell(name = "操作账号")
    private String createUsername;

    @XCell(name = "任务编号")
    private Long generateBatchId;
}
