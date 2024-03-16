package test.com.fcb.coupon.common.excel.importer;

import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import lombok.Data;


@Data
@XSheet(name = "测试表",titleRowNum = 1,desc = "注意：为保证执行效率，建议添加行数不超过10000行。 ---请勿删除此行，否则会引起解析异常，导致发券失败")
public class ImporterBean {

    @XCell(name = "* 第三方券码")
    private String couponCode;
    @XCell(name = "密码")
    private String password;

}
