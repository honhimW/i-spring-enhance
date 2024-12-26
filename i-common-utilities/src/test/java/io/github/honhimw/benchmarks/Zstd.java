package io.github.honhimw.benchmarks;

import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;

import java.io.ByteArrayOutputStream;

/**
 * Require implementation 'com.github.luben:zstd-jni'
 * @author hon_him
 * @since 2024-12-20
 */

public class Zstd extends ApacheBase {

    public int level;

    public Zstd() {
        this(3);
    }

    public Zstd(int level) {
        this.level = level;
    }

    @Override
    protected String kind() {
        return CompressorStreamFactory.getZstandard();
    }

    @Override
    protected CompressorOutputStream<?> createCompressorOutputStream(ByteArrayOutputStream baops) throws Exception {
        return new ZstdCompressorOutputStream(baops, level, true);
    }

}
