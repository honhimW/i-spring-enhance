package io.github.honhimw.test;

import io.github.honhimw.benchmarks.*;
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

public class CompressBenchMarkersTests {

    @Test
    @SneakyThrows
    void runGzipBenchMarks() {
        Options options = new OptionsBuilder()
//            .include(JdkGzip.class.getSimpleName())
            .include(ApacheGzip.class.getSimpleName())
            .include(Zstd.class.getSimpleName())
//            .include(XZ.class.getSimpleName())
//            .include(Lzma.class.getSimpleName())
//            .include(SnappyFramed.class.getSimpleName())
//            .include(Deflate.class.getSimpleName())
//            .include(Lz4Block.class.getSimpleName())
//            .include(Lz4Framed.class.getSimpleName())
//            .include(Bzip2.class.getSimpleName())
//            .include(Package200.class.getSimpleName())
            .forks(1)
            .resultFormat(ResultFormatType.LATEX)
            .build();

        new Runner(options).run();
    }

    @Test
    @SneakyThrows
    void runDirectly() {
        ApacheBase apache = new Lzma();
        apache.logging(true);
        apache.dataSize = 1024 * 1024 * 20;
        apache.setup();
        apache.compress();
        apache.decompress();
    }

}
