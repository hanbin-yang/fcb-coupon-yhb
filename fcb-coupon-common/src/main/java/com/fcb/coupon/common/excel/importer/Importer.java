package com.fcb.coupon.common.excel.importer;

import com.fcb.coupon.common.excel.bean.SheetParseResult;
import com.fcb.coupon.common.excel.excetption.ImportException;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author 唐陆军
 * @Description 导入接口类
 * @createTime 2021年06月18日 10:14:00
 */
public interface Importer {

    /*
     * @description 导入
     * @author 唐陆军

     * @param: inputStream excel、CSV文件的输入流
     * @param: rowClass 需要解析的bean的class
     * @date 2021-6-18 10:15

     * @return: com.fcb.coupon.common.excel.bean.SheetParseResult 解析结果
     */
    SheetParseResult parse(InputStream inputStream, Class rowClass) throws ImportException;


    /*
    解析，返回成功的结果列表
     */
    <T> List<T> parseSimple(InputStream inputStream, Class<T> rowClass) throws ImportException;

    SheetParseResult parse(InputStream inputStream, Class rowClass, Charset charset) throws ImportException;

    <T> List<T> parseSimple(InputStream inputStream, Class<T> rowClass, Charset charset) throws ImportException;
}
