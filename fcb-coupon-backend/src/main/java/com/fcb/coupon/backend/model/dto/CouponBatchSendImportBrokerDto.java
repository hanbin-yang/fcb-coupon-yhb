package com.fcb.coupon.backend.model.dto;

import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@XSheet(name = "批量发券机构经纪人信息导入",
        titleRowNum = 1,
        maxCount = 10000,
        desc = "注意：为保证执行效率，建议添加行数不超过10000行。 ---请勿删除此行，否则会引起解析异常，导致发券失败。"
)
public class CouponBatchSendImportBrokerDto {

    @NotBlank(message = "机构经纪人账号不能为空")
    @Size(max = 30, message = "机构经纪人账号不能超过30位")
    @XCell(name = "机构经纪人账号")
    private String phone;

    @NotNull(message = "发券数量不能为0")
    @Min(value = 1, message = "发券数量不能少于1")
    @XCell(name = "发券数量")
    private Integer count;
}
