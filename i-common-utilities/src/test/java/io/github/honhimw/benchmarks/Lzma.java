package io.github.honhimw.benchmarks;

import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;

/**
 * Require implementation 'org.tukaani:xz'
 * @author hon_him
 * @since 2024-12-20
 */

public class Lzma extends ApacheBase {

    @Override
    protected String kind() {
        return CompressorStreamFactory.getLzma();
    }

    @Override
    protected void doWrite(CompressorOutputStream<?> outputStream, byte[] data) throws Exception {
        super.doWrite(outputStream, data);
        if (outputStream instanceof LZMACompressorOutputStream ops) {
            ops.finish();
        }
    }

}
