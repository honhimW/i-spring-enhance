package io.github.honhimw.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BinaryCodec;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author hon_him
 * @since 2024-11-26
 */

public class Bytes {

    public static Bytes wrap(byte[] bytes) {
        return new Bytes(bytes);
    }

    public static Bytes fromStr(String str) {
        return new Bytes(str.getBytes(StandardCharsets.UTF_8));
    }

    public static Bytes fromStr(String str, Charset charset) {
        return new Bytes(str.getBytes(charset));
    }

    public static Bytes fromHex(String hex) {
        try {
            byte[] bytes = Hex.decodeHex(hex);
            return new Bytes(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Bytes fromBase64(String base64) {
        try {
            byte[] bytes = Base64.decodeBase64(base64);
            return new Bytes(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Bytes fromAscii(String ascii) {
        try {
            byte[] bytes = BinaryCodec.fromAscii(ascii.toCharArray());
            return new Bytes(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Bytes fromInputStream(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            return new Bytes(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Bytes fromByteBuffer(ByteBuffer byteBuffer) {
        try {
            byte[] bytes = byteBuffer.array();
            return new Bytes(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private final byte[] bytes;

    private Bytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] unwrap() {
        return bytes;
    }

    public String toString() {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String toString(Charset charset) {
        return new String(bytes, charset);
    }

    public String toHexString() {
        return Hex.encodeHexString(bytes);
    }

    public String toBase64() {
        return Base64.encodeBase64String(bytes);
    }

    public String toAscii() {
        return BinaryCodec.toAsciiString(bytes);
    }

    public InputStream toInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(bytes);
    }

}
