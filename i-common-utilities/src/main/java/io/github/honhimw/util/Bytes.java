package io.github.honhimw.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BinaryCodec;
import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2024-11-26
 */

public class Bytes {

    public static Bytes wrap(byte[] bytes) {
        return new Bytes(bytes);
    }

    public static Bytes fromStr(String str) {
        return wrap(str.getBytes(StandardCharsets.UTF_8));
    }

    public static Bytes fromStr(String str, Charset charset) {
        return wrap(str.getBytes(charset));
    }

    public static Bytes fromHex(String hex) {
        try {
            byte[] bytes = Hex.decodeHex(hex);
            return wrap(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Bytes fromBase64(String base64) {
        try {
            byte[] bytes = Base64.decodeBase64(base64);
            return wrap(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Bytes fromAscii(String ascii) {
        try {
            byte[] bytes = BinaryCodec.fromAscii(ascii.toCharArray());
            return wrap(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Bytes fromInputStream(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            return wrap(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Bytes fromByteBuffer(ByteBuffer byteBuffer) {
        try {
            byte[] bytes = byteBuffer.array();
            return wrap(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Bytes fromObject(Object object) {
        if (!(object instanceof Serializable)) {
            throw new IllegalArgumentException("object is not serializable");
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(object);
            oos.flush();
            byte[] bytes = out.toByteArray();
            return wrap(bytes);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private final byte[] bytes;

    private Bytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] unwrap() {
        return this.bytes;
    }

    public int length() {
        return this.bytes.length;
    }

    /**
     * Copy a new byte array from current byte array.
     *
     * @param start start index, must between `0` and `array.length`
     * @param stop  stop index, `-1` means last index of array
     * @return new instance of new byte array
     */
    public Bytes copy(int start, int stop) {
        int max = this.length() - 1;
        if (stop < 0) {
            stop = max;
        }
        byte[] newBytes = new byte[stop - start];
        System.arraycopy(this.bytes, start, newBytes, start, stop - start);
        return wrap(newBytes);
    }

    public Bytes map(Function<byte[], byte[]> mapper) {
        byte[] bytes = mapper.apply(this.bytes);
        return wrap(bytes);
    }

    public String asString() {
        return new String(this.bytes, StandardCharsets.UTF_8);
    }

    public String asString(Charset charset) {
        return new String(this.bytes, charset);
    }

    public String asHex() {
        return Hex.encodeHexString(this.bytes);
    }

    public String asBase64() {
        return Base64.encodeBase64String(this.bytes);
    }

    public String asAscii() {
        return BinaryCodec.toAsciiString(this.bytes);
    }

    public InputStream asInputStream() {
        return new ByteArrayInputStream(this.bytes);
    }

    public ByteBuffer asByteBuffer() {
        return ByteBuffer.wrap(this.bytes);
    }

    @SuppressWarnings("unchecked")
    public <T> T asObject() {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(this.bytes);
            ObjectInputStream ois = new ObjectInputStream(in);
            Object object = ois.readObject();
            return (T) object;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return "Bytes[" + this.length() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Bytes bytes1 = (Bytes) o;
        return Objects.deepEquals(this.bytes, bytes1.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

}
