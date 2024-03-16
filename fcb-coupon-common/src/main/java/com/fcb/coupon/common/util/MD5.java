package com.fcb.coupon.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	private static final Logger logger = LoggerFactory.getLogger(MD5.class);
	private final static char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static String bytesToHex(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		int t;
		for (int i = 0; i < 16; i++) {
			t = bytes[i];
			if (t < 0) {
				t += 256;
			}
			sb.append(HEX_DIGITS[(t >>> 4)]);
			sb.append(HEX_DIGITS[(t % 16)]);
		}
		return sb.toString();
	}

	public static String md5(String input) throws Exception {
		return code(input, 32);
	}

	public static String code(String input, int bit) throws Exception {
		try {
			MessageDigest md = MessageDigest.getInstance(System.getProperty(
					"MD5.algorithm", "MD5"));
			if (bit == 16) {
				return bytesToHex(md.digest(input.getBytes("utf-8")))
						.substring(8, 24);
			}
			return bytesToHex(md.digest(input.getBytes("utf-8")));
		} catch (NoSuchAlgorithmException e) {
			logger.error("不存在生成密码算法异常："+e.getMessage(),e);
			throw new Exception("Could not found MD5 algorithm.", e);
		}
	}
}
