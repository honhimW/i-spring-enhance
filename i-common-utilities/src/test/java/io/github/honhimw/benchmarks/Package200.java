package io.github.honhimw.benchmarks;

import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorOutputStream;

/**
 * Require
 * @author hon_him
 * @since 2024-12-20
 */

public class Package200 extends ApacheBase {

    @Override
    protected String kind() {
        return CompressorStreamFactory.getPack200();
    }

    @Override
    protected void doWrite(CompressorOutputStream<?> outputStream, byte[] data) throws Exception {
        super.doWrite(outputStream, data);
        if (outputStream instanceof Pack200CompressorOutputStream ops) {
            ops.finish();
        }
    }

}
