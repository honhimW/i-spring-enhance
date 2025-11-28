package io.github.honhimw.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author honhimW
 * @since 2025-07-15
 */

public class Blob {

    public final String name;

    public final String contentType;

    private byte[] blob;

    private InputStream inputStream;

    private Blob(String name, String contentType, byte[] blob, InputStream inputStream) {
        this.name = name;
        this.contentType = contentType;
        this.blob = blob;
        this.inputStream = inputStream;
    }

    public static Blob of(String name, String contentType, byte[] blob) {
        return new Blob(name, contentType, blob, null);
    }

    public static Blob of(String name, String contentType, InputStream inputStream) {
        return new Blob(name, contentType, null, inputStream);
    }

    public byte[] getBlob() {
        if (blob == null) {
            try {
                blob = inputStream.readAllBytes();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return blob;
    }

    public InputStream asInputStream() {
        if (inputStream == null) {
            inputStream = new ByteArrayInputStream(blob);
        }
        return inputStream;
    }

    public Bytes toBytes() {
        return Bytes.wrap(getBlob());
    }

}
