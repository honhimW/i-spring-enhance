package io.github.honhimw.spring.web.common.resolver.reactive;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.github.honhimw.spring.IDataBufferUtils;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.spring.web.common.resolver.annotation.CsvField;
import io.github.honhimw.util.GZipUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2024-07-18
 */

public class CsvJacksonNodeReactiveCustomizer implements JacksonNodeReactiveCustomizer {

    private final MediaType mediaType;

    private final CsvMapper CSV_MAPPER;

    private final CsvSchema CSV_SCHEMA;

    public CsvJacksonNodeReactiveCustomizer(ObjectMapper objectMapper) {
        this((CsvMapper) new CsvMapper().setSerializerFactory(objectMapper.getSerializerFactory()));
    }

    public CsvJacksonNodeReactiveCustomizer(CsvMapper csvMapper) {
        this(csvMapper, MediaType.parseMediaType("application/csv"));
    }

    public CsvJacksonNodeReactiveCustomizer(CsvMapper csvMapper, MediaType mediaType) {
        csvMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.CSV_MAPPER = csvMapper;
        this.mediaType = mediaType;
        this.CSV_SCHEMA = csvMapper.schemaWithHeader();
    }

    @Override
    public Mono<Void> customize(ObjectNode objectNode, MethodParameter parameter, ServerHttpRequest request) {
        if (!guard(objectNode, parameter, request)) {
            return Mono.empty();
        }
        TextParam parameterAnnotation = parameter.getParameterAnnotation(TextParam.class);
        CsvField csvField = parameter.getParameterAnnotation(CsvField.class);
        Assert.state(parameterAnnotation != null, "argument resolver annotation should not be null.");
        Assert.state(csvField != null, "csv field annotation should not be null.");
        String fieldName = csvField.value();
        Assert.state(StringUtils.isNotBlank(fieldName), "csv field annotation should not be null.");


        return IDataBufferUtils.fluxData2Bytes(request.getBody())
            .map(bytes -> tryDecompressGzip(parameterAnnotation, request, bytes))
            .map(bytes -> {
                Charset charset = Optional.of(request.getHeaders())
                    .map(HttpHeaders::getContentType)
                    .map(MimeType::getCharset)
                    .orElse(Charset.forName("GBK"));
                if (bytes.length > 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                    charset = StandardCharsets.UTF_8;
                }
                return new String(bytes, charset);
            })
            .doOnNext(jsonStr -> injectBodyParam(fieldName, objectNode, jsonStr))
            .then();
    }

    protected boolean guard(ObjectNode objectNode, MethodParameter parameter, ServerHttpRequest serverHttpRequest) {
        return mediaType.isCompatibleWith(serverHttpRequest.getHeaders().getContentType()) &&
               parameter.hasParameterAnnotation(CsvField.class);
    }

    protected void injectBodyParam(String fieldName, ObjectNode objectNode, String csvBody) {
        try (MappingIterator<ObjectNode> iterator = CSV_MAPPER.readerFor(ObjectNode.class).with(CSV_SCHEMA).readValues(csvBody)) {
            iterator.forEachRemaining(node -> objectNode.withArray("/" + fieldName).add(node));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected byte[] tryDecompressGzip(TextParam annotation, ServerHttpRequest request, byte[] body) {
        String contentEncoding = request.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        if (annotation.gzip() && StringUtils.equals(contentEncoding, "gzip")) {
            try {
                return GZipUtils.decompress(body);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            return body;
        }
    }
}
