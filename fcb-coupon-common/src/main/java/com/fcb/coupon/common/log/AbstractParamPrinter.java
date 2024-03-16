package com.fcb.coupon.common.log;

import com.fcb.coupon.common.dto.EmptyMultipartFile;
import com.fcb.coupon.common.util.ServletUtils;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月16日 14:15:00
 */
public abstract class AbstractParamPrinter {


    public Object processParam(Object src) throws IllegalAccessException, InstantiationException {
        if (src == null) {
            return src;
        }
        if (src instanceof javax.servlet.ServletRequest) {
            return src.getClass().getName();
        }
        if (src instanceof javax.servlet.ServletResponse) {
            return src.getClass().getName();
        }

        if (!ServletUtils.isMultipart()) {
            return src;
        }

        if (src instanceof MultipartFile) {
            return new EmptyMultipartFile((MultipartFile) src);
        }

        if (src.getClass().isArray()) {
            List<EmptyMultipartFile> mfs = new ArrayList();
            for (Object obj : (Object[]) src) {
                mfs.add(new EmptyMultipartFile((MultipartFile) obj));
            }
            return mfs;
        }

        if (src instanceof List) {
            List<EmptyMultipartFile> mfs = new ArrayList();
            for (Object obj : (ArrayList) src) {
                mfs.add(new EmptyMultipartFile((MultipartFile) obj));
            }
            return mfs;
        }

        Class<?> targetClass = src.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        Object copyObject = targetClass.newInstance();
        for (Field f : declaredFields) {
            Class<?> type = f.getType();
            f.setAccessible(true);
            if (type.isAssignableFrom(MultipartFile.class)) {
                f.set(copyObject, null);
            } else {
                f.set(copyObject, f.get(src));
            }
        }
        return copyObject;
    }
}
