package io.github.honhimw.spring;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Remember to check release the buffer after use.
 *
 * <pre>
 * ReactiveHttpOutputMessage#writeWith(Publisher) will auto release the buffer
 * ReactiveHttpOutputMessage#writeAndFlushWith(Publisher) will auto release the buffer
 * </pre>
 *
 * @author hon_him
 * @since 2022-06-15
 */
public class IDataBufferUtils {

    private IDataBufferUtils() {
    }

    public static final DefaultDataBufferFactory DEFAULT_DATA_BUFFER_FACTORY = new DefaultDataBufferFactory();

    public static Mono<DataBuffer> wrap2Mono(String data) {
        return Mono.just(wrap(DEFAULT_DATA_BUFFER_FACTORY, data));
    }

    public static Mono<DataBuffer> wrap2Mono(DataBufferFactory dataBufferFactory, String data) {
        return Mono.just(wrap(dataBufferFactory, data));
    }

    private static DataBuffer wrap(DataBufferFactory dataBufferFactory, String data) {
        return wrap(dataBufferFactory, data.getBytes(StandardCharsets.UTF_8));
    }

    public static DataBuffer wrap(DataBufferFactory dataBufferFactory, byte[] data) {
        return dataBufferFactory.wrap(data);
    }

    public static DataBuffer reduce(DataBuffer db1, DataBuffer db2) {
        return reduce(DEFAULT_DATA_BUFFER_FACTORY, db1, db2);
    }

    public static DataBuffer reduce(DataBufferFactory dataBufferFactory, DataBuffer db1, DataBuffer db2) {
        return dataBufferFactory.join(List.of(db1, db2));
    }

    public static Mono<byte[]> fluxData2Bytes(Flux<DataBuffer> dataBuffer) {
        return fluxData2Bytes(DEFAULT_DATA_BUFFER_FACTORY, dataBuffer);
    }

    public static Mono<byte[]> fluxData2Bytes(DataBufferFactory dataBufferFactory, Flux<DataBuffer> dataBuffer) {
        return dataBuffer
            .reduce((df1, df2) -> reduce(dataBufferFactory, df1, df2))
            .map(IDataBufferUtils::dataBuffer2Bytes);
    }

    public static byte[] dataBuffer2Bytes(DataBuffer dataBuffer) {
        return dataBuffer2Bytes(dataBuffer, true);
    }

    public static byte[] dataBuffer2Bytes(DataBuffer dataBuffer, boolean release) {
        int len = dataBuffer.readableByteCount();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(len);
        try {
            dataBuffer.toByteBuffer(byteBuffer);
            byte[] bs = new byte[byteBuffer.remaining()];
            byteBuffer.get(bs, 0, bs.length);
            return bs;
        } catch (Exception e) {
            throw new WrappedException(e);
        } finally {
            byteBuffer.clear();
            if (release) {
                DataBufferUtils.release(dataBuffer);
            }
        }
    }

}
