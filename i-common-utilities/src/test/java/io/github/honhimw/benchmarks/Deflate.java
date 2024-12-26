package io.github.honhimw.benchmarks;

import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;

/**
 * Require
 * @author hon_him
 * @since 2024-12-20
 */

public class Deflate extends ApacheBase {

    @Override
    protected String kind() {
        return CompressorStreamFactory.getDeflate();
    }

    @Override
    protected void doWrite(CompressorOutputStream<?> outputStream, byte[] data) throws Exception {
        super.doWrite(outputStream, data);
        if (outputStream instanceof DeflateCompressorOutputStream ops) {
            ops.finish();
        }
    }

}
