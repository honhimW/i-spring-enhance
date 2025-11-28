package io.github.honhimw.test;

import io.github.honhimw.benchmarks.http.Apache5;
import io.github.honhimw.benchmarks.http.Okhttp5;
import io.github.honhimw.benchmarks.http.Okhttp5PerRequest;
import io.github.honhimw.benchmarks.http.Reactive;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author hon_him
 * @since 2024-12-20
 */

public class HttpBenchMarkersTests {

    @Test
    @SneakyThrows
    void runHttpBenchMarks() {
        Options options = new OptionsBuilder()
            .include(Apache5.class.getSimpleName())
            .include(Reactive.class.getSimpleName())
            .include(Okhttp5.class.getSimpleName())
            .include(Okhttp5PerRequest.class.getSimpleName())
            .threads(8)
            .forks(1)
            .resultFormat(ResultFormatType.LATEX)
            .build();

        new Runner(options).run();
    }

}
