package io.github.honhimw.util;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author hon_him
 * @since 2024-11-18
 */

public class Base64Utils {

    public static String encode(String str) {
        return encode(str, StandardCharsets.UTF_8);
    }

    public static String encode(String str, Charset charset) {
        return Base64.encodeBase64String(str.getBytes(charset));
    }

    public static String decode(String str) {
        return decode(str, StandardCharsets.UTF_8);
    }

    public static String decode(String str, Charset charset) {
        return new String(Base64.decodeBase64(str.getBytes(charset)), charset);
    }

}
