package io.github.honhimw.benchmarks;

import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

/**
 * Require
 * @author hon_him
 * @since 2024-12-20
 */

public class Bzip2 extends ApacheBase {

    @Override
    protected String kind() {
        return CompressorStreamFactory.getBzip2();
    }

    @Override
    protected void doWrite(CompressorOutputStream<?> outputStream, byte[] data) throws Exception {
        super.doWrite(outputStream, data);
        if (outputStream instanceof BZip2CompressorOutputStream ops) {
            ops.finish();
        }
    }

}
