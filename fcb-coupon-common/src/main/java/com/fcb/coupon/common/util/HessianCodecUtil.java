package com.fcb.coupon.common.util;

import com.caucho.hessian.io.SerializerFactory;
import com.fcb.coupon.common.midplatform.SoaHessianInput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

/**
 * Hessian编解码工具
 *
 * @author HanBin_Yang
 * @since 2021/8/2 08:20
 */
public class HessianCodecUtil {
    private static final SerializerFactory factory = new SerializerFactory();

    /**
     * 反序列化
     *
     * @param b   序列数据
     * @param cls 承接类型
     * @param <T> 返回类型
     * @return 类型对象
     */
    public static <T> T decode(byte[] b, Class<T> cls) {
        if (b == null || cls == null) {
            return null;
        }
        byte[] content = new byte[b.length - 4];
        ByteBuffer buffer = ByteBuffer.wrap(b);
        int flag = buffer.getInt();
        buffer.get(content);
        if (content.length == 0) {
            return null;
        }

        if ((flag & 2) == 2) {
            content = unzip(content);
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        SoaHessianInput hessianInput = new SoaHessianInput(inputStream, factory);
        try {
            @SuppressWarnings("unchecked")
            T obj = (T) hessianInput.readObject();
            return obj;
        } catch (IOException e) {
            throw new RuntimeException("ValueDecodeUtil deserializeByHessian error", e);
        }
    }

    private static byte[] unzip(byte[] bytes) throws RuntimeException {
        if (bytes == null) {
            return null;
        } else {
            GZIPInputStream gzipInputStream = null;
            ByteArrayOutputStream bos = null;
            byte[] temp = new byte[2048];

            try {
                bos = new ByteArrayOutputStream(bytes.length);
                gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes));

                int length;
                while ((length = gzipInputStream.read(temp)) != -1) {
                    bos.write(temp, 0, length);
                }

                bytes = bos.toByteArray();
                return bytes;
            } catch (Exception var16) {
                throw new RuntimeException("unzip exception", var16);
            } finally {
                if (gzipInputStream != null) {
                    try {
                        gzipInputStream.close();
                    } catch (Exception var15) {
                    }
                }

                if (bos != null) {
                    try {
                        bos.close();
                    } catch (Exception var14) {

                    }
                }

            }
        }
    }
}
