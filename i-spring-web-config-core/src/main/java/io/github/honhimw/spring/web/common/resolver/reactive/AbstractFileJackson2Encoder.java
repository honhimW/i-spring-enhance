package io.github.honhimw.spring.web.common.resolver.reactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.spring.annotation.resolver.FileReturn;
import io.github.honhimw.spring.web.reactive.WebFluxJackson2Encoder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2024-08-12
 */

public abstract class AbstractFileJackson2Encoder extends WebFluxJackson2Encoder {

    public AbstractFileJackson2Encoder(ObjectMapper mapper, MimeType... mimeTypes) {
        super(mapper, mimeTypes);
    }

    @Nonnull
    @Override
    public DataBuffer encodeValue(@Nonnull Object value,
                                  @Nonnull DataBufferFactory bufferFactory,
                                  @Nonnull ResolvableType valueType,
                                  @Nullable MimeType mimeType,
                                  @Nullable Map<String, Object> hints) {
        DataBuffer valueDataBuffer = null;
        MethodParameter parameter = getParameter(valueType);
        if (Objects.nonNull(parameter) && parameter.hasMethodAnnotation(FileReturn.class)) {
            FileReturn fileReturn = parameter.getMethodAnnotation(FileReturn.class);
            Assert.notNull(fileReturn, "fileReturn should not be null");
            FileReturn.Encoding encoding = fileReturn.encoding();

            if (Objects.nonNull(mimeType)) {
                Map<String, String> parameters = new LinkedHashMap<>(mimeType.getParameters());
                parameters.put("charset", encoding.getCharset().name());
                mimeType = new MediaType(mimeType.getType(), mimeType.getSubtype(), parameters);
            }

            valueDataBuffer = bufferFactory.join(List.of(
                bufferFactory.wrap(encoding.getPrefix()),
                super.encodeValue(value, bufferFactory, valueType, mimeType, hints),
                bufferFactory.wrap(encoding.getSuffix())
            ));
        } else {
            valueDataBuffer = super.encodeValue(value, bufferFactory, valueType, mimeType, hints);
        }
        return valueDataBuffer;
    }
}
