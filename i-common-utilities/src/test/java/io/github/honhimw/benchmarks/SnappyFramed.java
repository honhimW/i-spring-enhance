package io.github.honhimw.benchmarks;

import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 * Require
 * @author hon_him
 * @since 2024-12-20
 */

public class SnappyFramed extends ApacheBase {

    @Override
    protected String kind() {
        return CompressorStreamFactory.getSnappyFramed();
    }

}
