package io.github.honhimw.spring;

import org.springframework.core.io.buffer.CloseableDataBuffer;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DataBufferWrapper;

/**
 * @author hon_him
 * @since 2024-07-15
 */

public class IDataBufferWrapper extends DataBufferWrapper implements CloseableDataBuffer {

    /**
     * Create a new {@code DataBufferWrapper} that wraps the given buffer.
     *
     * @param delegate the buffer to wrap
     */
    public IDataBufferWrapper(DataBuffer delegate) {
        super(delegate);
    }

    @Override
    public void close() {
        DataBufferUtils.release(dataBuffer());
    }

}
