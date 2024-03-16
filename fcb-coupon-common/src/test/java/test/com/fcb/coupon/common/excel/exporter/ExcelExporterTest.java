package test.com.fcb.coupon.common.excel.exporter;

import com.fcb.coupon.common.excel.constant.ImportConstant;
import com.fcb.coupon.common.excel.exporter.ExcelExporter;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 16:36:00
 */
public class ExcelExporterTest {


    @Test
    public void testExport() {
        String rootPath = this.getClass().getResource("/").getPath();
        File file = null;
        try {
            ExporterBean exporterBean = new ExporterBean();
            exporterBean.setAmount(BigDecimal.valueOf(100));
            exporterBean.setBirthday(new Date());
            exporterBean.setName("测试");
            exporterBean.setPhone("13545456767");
            exporterBean.setBirthday(new Date());
            List<Object> list = new ArrayList<>();
            list.add(exporterBean);

            file = new File(rootPath + "test" + ImportConstant.EXCEL_2007_SUFFIX);
            OutputStream outputStream = new FileOutputStream(file);
            new ExcelExporter().export(outputStream, ExporterBean.class, list);
        } catch (Exception ex) {
            assert false;
        } finally {
            file.delete();
        }
    }
}
