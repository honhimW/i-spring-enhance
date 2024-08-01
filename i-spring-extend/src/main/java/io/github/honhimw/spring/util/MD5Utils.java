package io.github.honhimw.spring.util;

import java.security.MessageDigest;

/**
 * @author hon_him
 * @since 2022-06-06
 */
public class MD5Utils {

    private static final char[] HEX_DIGITS_LOWER_CAST =
        "0123456789abcdef".toCharArray();

    private static final char[] HEX_DIGITS_UPPER_CAST =
        "0123456789ABCDEF".toCharArray();

    public static String getMD5(String str) {
        return md5(str, HEX_DIGITS_LOWER_CAST);
    }

    public static String getMD5UpperCast(String str) {
        return md5(str, HEX_DIGITS_UPPER_CAST);
    }
    private static String md5(String str, char[] cast) {
        try {
            byte[] bytes = str.getBytes();
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(bytes);
            byte[] digest = messageDigest.digest();
            char[] chars = new char[32];
            int k = 0;
            for (byte b : digest) {
                chars[k++] = cast[b >>> 4 & 0xf];
                chars[k++] = cast[b & 0xf];
            }
            return new String(chars);
        } catch (Exception ignored) {
            return "";
        }
    }

}
