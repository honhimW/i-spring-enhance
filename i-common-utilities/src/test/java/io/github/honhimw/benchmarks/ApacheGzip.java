package io.github.honhimw.benchmarks;

import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

/**
 * @author hon_him
 * @since 2024-12-20
 */

public class ApacheGzip extends ApacheBase {

    @Override
    protected String kind() {
        return CompressorStreamFactory.getGzip();
    }

    @Override
    protected void doWrite(CompressorOutputStream<?> outputStream, byte[] data) throws Exception {
        super.doWrite(outputStream, data);
        if (outputStream instanceof GzipCompressorOutputStream ops) {
            ops.finish();
        }
    }

}
