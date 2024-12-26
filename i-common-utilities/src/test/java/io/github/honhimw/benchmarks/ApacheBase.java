package io.github.honhimw.benchmarks;

import lombok.SneakyThrows;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamProvider;
import org.openjdk.jmh.annotations.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * @author hon_him
 * @since 2024-12-20
 */

public abstract class ApacheBase extends AbstractCompressBenchMarks {

    private CompressorStreamProvider provider;

    private boolean logging;

    public void logging(boolean enabled) {
        logging = enabled;
    }

    @Override
    public void setup() {
        SortedMap<String, CompressorStreamProvider> providers = CompressorStreamFactory.findAvailableCompressorInputStreamProviders();
        provider = providers.get(_kind());
        Objects.requireNonNull(provider, "No compressor found for " + _kind());
        super.setup();
    }

    @Override
    public byte[] generateCompressedData(int size) throws Exception {
        byte[] data = generateRandomData(size);
        try (
            ByteArrayOutputStream baops = new ByteArrayOutputStream()) {
            CompressorOutputStream<?> outputStream = createCompressorOutputStream(baops);
            doWrite(outputStream, data);
            return baops.toByteArray();
        }
    }

    protected CompressorOutputStream<?> createCompressorOutputStream(ByteArrayOutputStream baops) throws Exception {
        return provider.createCompressorOutputStream(_kind(), baops);
    }

    protected void doWrite(CompressorOutputStream<?> outputStream, byte[] data) throws Exception {
        outputStream.write(data);
        outputStream.flush();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    @Fork(1)
    @SneakyThrows
    @Override
    public void compress() {
        try (
            ByteArrayOutputStream baops = new ByteArrayOutputStream();
            CompressorOutputStream<?> outputStream = createCompressorOutputStream(baops)) {
            outputStream.write(noneCompressedData);
            if (logging) {
                System.out.printf("%s[C]: %s%n", getClass().getSimpleName(), baops.toByteArray().length);
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    @Fork(1)
    @SneakyThrows
    @Override
    public void decompress() {
        try (ByteArrayInputStream baips = new ByteArrayInputStream(compressedData);
             CompressorInputStream inputStream = provider.createCompressorInputStream(_kind(), baips, false);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = getBuffer();
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            if (logging) {
                System.out.printf("%s[D]: %s%n", getClass().getSimpleName(), outputStream.toByteArray().length);
            }
        }
    }

    protected abstract String kind();

    private String _kind() {
        return kind().toUpperCase(Locale.ROOT);
    }

}
