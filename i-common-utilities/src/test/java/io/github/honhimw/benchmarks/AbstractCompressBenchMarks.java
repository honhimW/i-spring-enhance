package io.github.honhimw.benchmarks;

import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.openjdk.jmh.annotations.*;

import java.nio.charset.StandardCharsets;

/**
 * @author hon_him
 * @since 2024-12-20
 */

@State(Scope.Benchmark)
public abstract class AbstractCompressBenchMarks {

    @Param({"2048", "524288"})
    public int dataSize;

    protected byte[] noneCompressedData;

    protected byte[] compressedData;

    @SneakyThrows
    @Setup(Level.Trial)
    public void setup() {
        noneCompressedData = generateRandomData(dataSize);
        compressedData = generateCompressedData(dataSize);
    }

    public byte[] generateRandomData(int size) {
        return RandomStringUtils.insecure().nextAlphanumeric(size).getBytes(StandardCharsets.UTF_8);
    }

    public abstract byte[] generateCompressedData(int size) throws Exception;

    public byte[] getBuffer() {
        return new byte[8192];
    }

    public abstract void compress();

    public abstract void decompress();

}
