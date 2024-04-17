package com.sensorsdata.analytics.android.sdk.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 该类主要用于
 *
 * @author wwm
 * @version 1.0
 * @since 2024/4/16
 */

public final class EncryptUtils {
	public static String encryptMD5ToString(final String data) {
		if (data == null || data.length() == 0) return "";
		return encryptMD5ToString(data.getBytes());
	}

	public static String encryptMD5ToString(final byte[] data) {
		return bytes2HexString(encryptMD5(data));
	}

	public static byte[] encryptMD5(final byte[] data) {
		return hashTemplate(data, "MD5");
	}

	static byte[] hashTemplate(final byte[] data, final String algorithm) {
		if (data == null || data.length <= 0) return null;
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.update(data);
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String bytes2HexString(final byte[] bytes) {
		return bytes2HexString(bytes, true);
	}

	private static final char[] HEX_DIGITS_UPPER =
			{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	private static final char[] HEX_DIGITS_LOWER =
			{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	public static String bytes2HexString(final byte[] bytes, boolean isUpperCase) {
		if (bytes == null) return "";
		char[] hexDigits = isUpperCase ? HEX_DIGITS_UPPER : HEX_DIGITS_LOWER;
		int len = bytes.length;
		if (len <= 0) return "";
		char[] ret = new char[len << 1];
		for (int i = 0, j = 0; i < len; i++) {
			ret[j++] = hexDigits[bytes[i] >> 4 & 0x0f];
			ret[j++] = hexDigits[bytes[i] & 0x0f];
		}
		return new String(ret);
	}
}
