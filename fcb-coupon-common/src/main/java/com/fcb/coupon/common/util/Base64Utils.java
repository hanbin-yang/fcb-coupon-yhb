package com.fcb.coupon.common.util;

import org.apache.xmlbeans.impl.util.Base64;

import java.io.*;

/**
 * @author YangHanBin
 * @date 2021-08-17 10:30
 */
public class Base64Utils {
    private static final int CACHE_SIZE = 1024;

    public Base64Utils() {
    }

    public static byte[] decode(String base64) throws Exception {
        return Base64.decode(base64.getBytes("utf-8"));
    }

    public static String encode(byte[] bytes) throws Exception {
        return new String(Base64.encode(bytes), "utf-8");
    }

    public static String encodeFile(String filePath) throws Exception {
        byte[] bytes = fileToByte(filePath);
        return encode(bytes);
    }

    public static void decodeToFile(String filePath, String base64) throws Exception {
        byte[] bytes = decode(base64);
        byteArrayToFile(bytes, filePath);
    }

    public static byte[] fileToByte(String filePath) throws Exception {
        byte[] data = new byte[0];
        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream in = null;
            ByteArrayOutputStream out = null;

            try {
                in = new FileInputStream(file);
                out = new ByteArrayOutputStream(2048);
                byte[] cache = new byte[1024];
                boolean var6 = false;

                int nRead;
                while((nRead = in.read(cache)) != -1) {
                    out.write(cache, 0, nRead);
                    out.flush();
                }

                data = out.toByteArray();
            } finally {
                if (null != in) {
                    in.close();
                }

                if (null != out) {
                    out.close();
                }

            }
        }

        return data;
    }

    public static void byteArrayToFile(byte[] bytes, String filePath) throws Exception {
        ByteArrayInputStream in = null;

        try {
            in = new ByteArrayInputStream(bytes);
            File destFile = new File(filePath);
            if (!destFile.getParentFile().exists() && !destFile.getParentFile().mkdirs()) {
                throw new RuntimeException("创建 " + filePath + " 父级目录异常");
            }

            if (destFile.createNewFile()) {
                FileOutputStream out = null;

                try {
                    out = new FileOutputStream(destFile);
                    byte[] cache = new byte[1024];
                    boolean var6 = false;

                    int nRead;
                    while((nRead = in.read(cache)) != -1) {
                        out.write(cache, 0, nRead);
                        out.flush();
                    }
                } catch (Exception var15) {
                    throw new RuntimeException("写入 " + filePath + " 字节流异常", var15);
                } finally {
                    if (null != out) {
                        out.close();
                    }

                }
            }
        } finally {
            if (null != in) {
                in.close();
            }

        }

    }
}

