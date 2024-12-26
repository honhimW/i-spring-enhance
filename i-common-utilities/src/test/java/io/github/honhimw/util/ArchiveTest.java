package io.github.honhimw.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.concurrent.ThreadSafe;

/**
 * @author hon_him
 * @since 2024-12-25
 */

public class ArchiveTest {

    private static String RAW;

    @BeforeAll
    static void beforeAll() {
        try {
            RAW = HttpUtils.getInstance().get("https://www.apache.org/licenses/LICENSE-2.0.txt").str();
        } catch (Exception ignored) {
        }
    }

    @Test
    @SneakyThrows
    void gzip() {
        ArchiveUtils.Archiver gzip = ArchiveUtils.create(ArchiveUtils.Selector::gzip);
        Bytes bytes = Bytes.fromBase64(RAW);
        int uncompressedLength = bytes.unwrap().length;
        Bytes compress = gzip.compress(bytes);
        int compressedLength = compress.unwrap().length;
        System.out.println("GZIP[U] = " + uncompressedLength);
        System.out.println("GZIP[C] = " + compressedLength);
    }

    @Test
    @SneakyThrows
    void deflate() {
        ArchiveUtils.Archiver deflate = ArchiveUtils.create(ArchiveUtils.Selector::deflate);
        Bytes bytes = Bytes.fromBase64(RAW);
        int uncompressedLength = bytes.unwrap().length;
        Bytes compress = deflate.compress(bytes);
        int compressedLength = compress.unwrap().length;
        System.out.println("Deflate[U] = " + uncompressedLength);
        System.out.println("Deflate[C] = " + compressedLength);
    }

    @Test
    @SneakyThrows
    void xz() {
        ArchiveUtils.Archiver xz = ArchiveUtils.create(ArchiveUtils.Selector::xz);
        Bytes bytes = Bytes.fromBase64(RAW);
        int uncompressedLength = bytes.unwrap().length;
        Bytes compress = xz.compress(bytes);
        int compressedLength = compress.unwrap().length;
        System.out.println("XZ[U] = " + uncompressedLength);
        System.out.println("XZ[C] = " + compressedLength);
    }

    @Test
    @SneakyThrows
    void zstd() {
        ArchiveUtils.Archiver zstd = ArchiveUtils.create(ArchiveUtils.Selector::zstd);
        Bytes bytes = Bytes.fromBase64(RAW);
        int uncompressedLength = bytes.unwrap().length;
        Bytes compress = zstd.compress(bytes);
        int compressedLength = compress.unwrap().length;
        System.out.println("ZSTD[U] = " + uncompressedLength);
        System.out.println("ZSTD[C] = " + compressedLength);
    }

}
