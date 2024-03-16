package com.fcb.coupon.backend.model.param.response;

import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import lombok.Data;

import java.util.Date;

/**
 * @author 唐陆军
 * @Description 优惠券发送失败结果
 * @createTime 2021年08月10日 16:36:00
 */
@Data
@XSheet(name = "券明细列表1", titleRowNum = 0)
public class CouponSendErrorResponse {

    @XCell(name = "序号")
    private Integer index;
    @XCell(name = "优惠券名称")
    private String couponName;
    @XCell(name = "操作类型")
    private String oprtName;
    @XCell(name = "券活动时期")
    private String startEndTime;
    @XCell(name = "发放手机号")
    private String phone;
    @XCell(name = "导入状态")
    private String statusName;
    @XCell(name = "张数")
    private Integer count;
    @XCell(name = "操作时间")
    private Date createTime;
    @XCell(name = "操作账号")
    private String createUserName;
    @XCell(name = "任务编号")
    private String generateBatchId;
    @XCell(name = "备注")
    private String remark;
}
