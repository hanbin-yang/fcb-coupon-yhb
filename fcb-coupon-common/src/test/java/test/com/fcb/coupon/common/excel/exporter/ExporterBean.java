package test.com.fcb.coupon.common.excel.exporter;

import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 16:37:00
 */
@Data
@XSheet(name = "测试表",desc = "注意：为保证执行效率，建议添加行数不超过10000行。 ---请勿删除此行，否则会引起解析异常，导致发券失败")
public class ExporterBean {

    private static final long serialVersionUID = 1863935878346529051L;

    @XCell(name = "姓名")
    private String name;
    @XCell(name = "年龄")
    private Integer age;
    @XCell(name = "金额")
    private BigDecimal amount;
    @XCell(name = "手机")
    private String phone;
    @XCell(name = "生日")
    private Date birthday;

}
