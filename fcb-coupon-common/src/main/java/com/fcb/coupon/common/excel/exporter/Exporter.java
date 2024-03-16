package com.fcb.coupon.common.excel.exporter;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 19:20:00
 */
public interface Exporter {


    void export(OutputStream outputStream, Class rowClass, List<? extends Object> rowDatas);

    void export(OutputStream outputStream, Class rowClass, List<? extends Object> rowDatas, Charset charset);

}
