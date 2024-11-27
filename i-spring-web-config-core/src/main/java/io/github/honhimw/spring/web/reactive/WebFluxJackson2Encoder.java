package io.github.honhimw.spring.web.reactive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import io.github.honhimw.core.IResult;
import io.github.honhimw.spring.web.common.i18n.I18nUtils;
import io.github.honhimw.util.JsonUtils;
import jakarta.annotation.Nonnull;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.EncodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2CodecSupport;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author hon_him
 * @since 2023-04-15
 */

public class WebFluxJackson2Encoder extends FetcherJackson2Encoder {

    private static final ResolvableType STRING = ResolvableType.forClass(String.class);

    public WebFluxJackson2Encoder() {
        this(JsonUtils.mapper().copy()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setTimeZone(TimeZone.getDefault())
            , MediaType.APPLICATION_JSON);
    }

    public WebFluxJackson2Encoder(ObjectMapper mapper, MimeType... mimeTypes) {
        super(mapper, mimeTypes);
    }

    @Override
    public boolean canEncode(@Nonnull ResolvableType elementType, MimeType mimeType) {
        if (STRING.isAssignableFrom(elementType)) {
            return false;
        }
        return super.canEncode(elementType, mimeType);
    }

    @Nonnull
    @Override
    public Flux<DataBuffer> encode(@Nonnull Publisher<?> inputStream, @Nonnull DataBufferFactory bufferFactory, @Nonnull ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
        Assert.notNull(inputStream, "'inputStream' must not be null");
        Assert.notNull(bufferFactory, "'bufferFactory' must not be null");
        Assert.notNull(elementType, "'elementType' must not be null");

        if (inputStream instanceof Mono) {
            return Mono.from(inputStream)
                .map(value -> encodeValue(value, bufferFactory, elementType, mimeType, hints))
                .flux();
        } else {
            byte[] separator = getStreamingMediaTypeSeparator(mimeType);
            if (separator != null) { // streaming
                try {
                    ObjectMapper mapper = selectObjectMapper(elementType, mimeType);
                    if (mapper == null) {
                        throw new IllegalStateException("No ObjectMapper for " + elementType);
                    }
                    ObjectWriter writer = createObjectWriter(mapper, elementType, mimeType, null, hints);
                    ByteArrayBuilder byteBuilder = new ByteArrayBuilder(writer.getFactory()._getBufferRecycler());
                    Charset charset = getCharset(mimeType);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteBuilder, charset);
                    JsonGenerator generator = decorateGenerator(mapper.getFactory().createGenerator(outputStreamWriter));


                    return Flux.from(inputStream)
                        .doOnNext(this::i18n)
                        .<DataBuffer>handle((value, sink) -> {
                            try {
                                SequenceWriter sequenceWriter = customizeWriter(writer, elementType, value).writeValues(generator);
                                sink.next(encodeStreamingValue(value, bufferFactory, hints, sequenceWriter, byteBuilder,
                                    separator));
                            } catch (IOException e) {
                                sink.error(new RuntimeException(e));
                            }
                        })
                        .doAfterTerminate(() -> {
                            try {
                                byteBuilder.release();
                                generator.close();
                            } catch (IOException ex) {
                                logger.error("Could not close Encoder resources", ex);
                            }
                        });
                } catch (IOException ex) {
                    return Flux.error(ex);
                }
            } else { // non-streaming
                ResolvableType listType = ResolvableType.forClassWithGenerics(List.class, elementType);
                return Flux.from(inputStream)
                    .collectList()
                    .map(list -> encodeValue(list, bufferFactory, listType, mimeType, hints))
                    .flux();
            }

        }
    }

    @Nonnull
    @Override
    public DataBuffer encodeValue(@Nonnull Object value, @Nonnull DataBufferFactory bufferFactory, @Nonnull ResolvableType valueType, MimeType mimeType, Map<String, Object> hints) {
        Class<?> jsonView = null;
        FilterProvider filters = null;
        if (value instanceof MappingJacksonValue container) {
            value = container.getValue();
            valueType = ResolvableType.forInstance(value);
            jsonView = container.getSerializationView();
            filters = container.getFilters();
        }

        ObjectMapper mapper = selectObjectMapper(valueType, mimeType);
        if (mapper == null) {
            throw new IllegalStateException("No ObjectMapper for " + valueType);
        }

        ObjectWriter writer = createObjectWriter(mapper, valueType, mimeType, jsonView, hints);
        if (filters != null) {
            writer = writer.with(filters);
        }

        ByteArrayBuilder byteBuilder = new ByteArrayBuilder(writer.getFactory()._getBufferRecycler());
        try {
            Charset charset = getCharset(mimeType);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteBuilder, charset);

            JsonGenerator generator = mapper.getFactory().createGenerator(outputStreamWriter);
            generator = decorateGenerator(generator);
            try {
                i18n(value);
                writer = customizeWriter(writer, valueType, value);
                writer.writeValue(generator, value);
                generator.flush();
            } catch (InvalidDefinitionException ex) {
                throw new CodecException("Type definition error: " + ex.getType(), ex);
            } catch (JsonProcessingException ex) {
                throw new EncodingException("JSON encoding error: " + ex.getOriginalMessage(), ex);
            }

            byte[] bytes = byteBuilder.toByteArray();
            DataBuffer buffer = bufferFactory.allocateBuffer(bytes.length);
            buffer.write(bytes);
            Hints.touchDataBuffer(buffer, hints, logger);

            return buffer;
        } catch (IOException ex) {
            throw new IllegalStateException("Unexpected I/O error while writing to byte array builder", ex);
        } finally {
            byteBuilder.release();
        }
    }

    protected ObjectWriter createObjectWriter(
        ObjectMapper mapper, ResolvableType valueType, @Nullable MimeType mimeType,
        @Nullable Class<?> jsonView, @Nullable Map<String, Object> hints) {

        JavaType javaType = getJavaType(valueType.getType(), null);
        if (jsonView == null && hints != null) {
            jsonView = (Class<?>) hints.get(Jackson2CodecSupport.JSON_VIEW_HINT);
        }
        ObjectWriter writer = (jsonView != null ? mapper.writerWithView(jsonView) : mapper.writer());
        if (javaType.isContainerType()) {
            writer = writer.forType(javaType);
        }
        return customizeWriter(writer, mimeType, valueType, hints);
    }

    @Nonnull
    protected ObjectWriter customizeWriter(@Nonnull ObjectWriter writer, ResolvableType type, Object object) {
        return writer;
    }

    @Nonnull
    protected ObjectWriter customizeWriter(@Nonnull ObjectWriter writer, @Nullable MimeType mimeType,
                                           @Nonnull ResolvableType elementType, @Nullable Map<String, Object> hints) {
        return writer;
    }

    protected DataBuffer encodeStreamingValue(Object value, DataBufferFactory bufferFactory, @Nullable Map<String, Object> hints,
                                              SequenceWriter sequenceWriter, ByteArrayBuilder byteArrayBuilder, byte[] separator) {

        try {
            sequenceWriter.write(value);
            sequenceWriter.flush();
        } catch (InvalidDefinitionException ex) {
            throw new CodecException("Type definition error: " + ex.getType(), ex);
        } catch (JsonProcessingException ex) {
            throw new EncodingException("JSON encoding error: " + ex.getOriginalMessage(), ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Unexpected I/O error while writing to byte array builder", ex);
        }

        byte[] bytes = byteArrayBuilder.toByteArray();
        byteArrayBuilder.reset();

        int offset;
        int length;
        if (bytes.length > 0 && bytes[0] == ' ') {
            // SequenceWriter writes an unnecessary space in between values
            offset = 1;
            length = bytes.length - 1;
        } else {
            offset = 0;
            length = bytes.length;
        }
        DataBuffer buffer = bufferFactory.allocateBuffer(length + separator.length);
        buffer.write(bytes, offset, length);
        buffer.write(separator);
        Hints.touchDataBuffer(buffer, hints, logger);

        return buffer;
    }

    private void i18n(Object value) {
        if (value instanceof IResult<?> Result) {
            I18nUtils.i18n(Result);
        }
    }

    @Nonnull
    protected Charset getCharset(@Nullable MimeType contentType) {
        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        }
        else {
            return StandardCharsets.UTF_8;
        }
    }

}
