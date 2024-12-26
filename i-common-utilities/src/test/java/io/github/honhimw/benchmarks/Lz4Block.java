package io.github.honhimw.benchmarks;

import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;

/**
 * Require
 * @author hon_him
 * @since 2024-12-20
 */

public class Lz4Block extends ApacheBase {

    @Override
    protected String kind() {
        return CompressorStreamFactory.getLZ4Block();
    }

    @Override
    protected void doWrite(CompressorOutputStream<?> outputStream, byte[] data) throws Exception {
        super.doWrite(outputStream, data);
        if (outputStream instanceof BlockLZ4CompressorOutputStream ops) {
            ops.finish();
        }
    }

}
