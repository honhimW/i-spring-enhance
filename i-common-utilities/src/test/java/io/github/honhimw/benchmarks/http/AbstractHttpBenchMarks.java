package io.github.honhimw.benchmarks.http;

import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.openjdk.jmh.annotations.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author hon_him
 * @since 2024-12-20
 */

@State(Scope.Benchmark)
public abstract class AbstractHttpBenchMarks {

    @Param({"1024"})
//    @Param({"256", "512", "1024", "2048"})
    public int dataSize;

    protected String data;

    @SneakyThrows
    @Setup(Level.Trial)
    public void setup() {
        data = RandomStringUtils.insecure().nextAlphanumeric(dataSize);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    @Fork(1)
    public void get() throws Exception {
        throw new IllegalStateException();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 10, timeUnit = TimeUnit.SECONDS)
    @Fork(1)
    public void post() throws Exception {
        throw new IllegalStateException();
    }

}
