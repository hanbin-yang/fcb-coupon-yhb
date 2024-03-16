package test.com.fcb.coupon.common.excel.importer;

import com.fcb.coupon.common.excel.bean.SheetParseResult;
import com.fcb.coupon.common.excel.importer.ExcelImporter;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月18日 10:31:00
 */
public class ExcelImporterTest {

    @Test
    public void importTest() {
        Resource resource = new ClassPathResource("/test.xls");
        try {
            SheetParseResult sheetParseResult = new ExcelImporter().parse(resource.getInputStream(), ImporterBean.class);
            assert sheetParseResult.getRowParseResultMap().size() > 0;
        } catch (Exception ex) {
            assert false;
        }
    }

}
