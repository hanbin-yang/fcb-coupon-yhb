package com.fcb.coupon.backend.model.param.request;

import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月18日 10:29:00
 */
@Data
@XSheet(name = "第三方券码导入",
        titleRowNum = 1,
        desc = "注意：为保证执行效率，建议添加行数不超过10000行。 ---请勿删除此行，否则会引起解析异常，导致发券失败。",
        maxCount = 100000
)
public class CouponThirdImportRequest implements Serializable {


    @XCell(name = "第三方卡号/优惠券码")
    private String thirdCouponCode;

    @NotBlank(message = "密码不能为空")
    @XCell(name = "*密码")
    private String thirdCouponPassword;

}
