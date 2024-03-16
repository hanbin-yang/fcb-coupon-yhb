package test.com.fcb.coupon.common.excel.importer;

import com.fcb.coupon.common.excel.bean.SheetParseResult;
import com.fcb.coupon.common.excel.importer.CSVImporter;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import test.com.fcb.coupon.common.excel.exporter.ExporterBean;

import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 17:42:00
 */
public class CSVImporterTest {

    @Test
    public void testImportGBK() {
        Resource resource = new ClassPathResource("/test-gbk.csv");
        try {
            SheetParseResult sheetParseResult = new CSVImporter().parse(resource.getInputStream(), ExporterBean.class);
            assert sheetParseResult.getRowParseResultMap().size() > 0;
        } catch (Exception ex) {
            assert false;
        }
    }

    @Test
    public void testImportUTF8() {
        Resource resource = new ClassPathResource("/test.csv");
        try {
            SheetParseResult sheetParseResult = new CSVImporter().parse(resource.getInputStream(), ExporterBean.class);
            assert sheetParseResult.getRowParseResultMap().size() > 0;
        } catch (Exception ex) {
            assert false;
        }
    }

    @Test
    public void testImportSimple() {
        Resource resource = new ClassPathResource("/test.csv");
        try {
            List<ExporterBean> list = new CSVImporter().parseSimple(resource.getInputStream(), ExporterBean.class);
            assert list.size() > 0;
        } catch (Exception ex) {
            assert false;
        }
    }
}
