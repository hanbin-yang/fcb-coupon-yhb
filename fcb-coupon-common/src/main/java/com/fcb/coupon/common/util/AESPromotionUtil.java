package com.fcb.coupon.common.util;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 券码加密、解密工具类
 *
 * @Author WeiHaiQi
 * @Date 2021-06-17 14:49
 **/
public class AESPromotionUtil {

    private static final Logger logger = LoggerFactory.getLogger(AESPromotionUtil.class);
    //设置16位长度的密钥,保证与sql加密结果一致
    private static final String STRING_KEY = "cFOt;r4QzlkwjshU";

    /**
     * 加密
     *
     * @param content 需要加密的内容
     * @return 加密成功返回Base64编码的字符串,加密失败返回null,传入的内容已经加密过则直接返回原文
     */
    public static String encrypt(String content) {
        //为空直接返回null
        if (StringUtils.isBlank(content)) {
            return null;
        }
        //判断首字母是否为 @%^* ,如过为 @%^* 则说明是加密过的
        if (content.indexOf("@%^*") != 0) {
            try {
                SecretKey key = new SecretKeySpec(STRING_KEY.getBytes("UTF-8"),"AES");
                Cipher cipher = Cipher.getInstance("AES");// 创建密码器
                byte[] byteContent = content.getBytes("UTF-8");
                cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
                byte[] result = cipher.doFinal(byteContent);
                return "@%^*" +new String(Base64.encode(result),"UTF-8"); // 加密(最前面拼接特殊字符串)
            } catch (Exception e) {
                logger.error("Exception异常 | AESUtil.encrypt",e);
            }
            return null;
        }
        //已经加密过的直接返回content
        return content;
    }

    /**解密
     * @param content  待解密内容
     * @return 解密成功后返回解密后的明文,无需解密直接返回原文,解密失败返回null
     */
    public static String decrypt(String content) {
        //为空直接返回null
        if (StringUtils.isBlank(content)) {
            return null;
        }
        if (content.indexOf("@%^*") == 0) {
            //截取 @%^* 后面的内容作为需要解密的密文
            content = content.substring(4, content.length());
            try {
                SecretKey key = new SecretKeySpec(STRING_KEY.getBytes("UTF-8"),"AES");
                Cipher cipher = Cipher.getInstance("AES");// 创建密码器
                cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
                byte[] result = cipher.doFinal(Base64.decode(content));
                return new String(result,"UTF-8"); // 解密
            }catch (Exception e) {
                logger.error("Exception异常 | AESUtil.decrypt",e);
            }
            return null;
        }
        return content;
    }
}
