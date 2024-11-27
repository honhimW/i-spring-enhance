package io.github.honhimw.spring.web.common.resolver.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.honhimw.spring.IDataBufferUtils;
import io.github.honhimw.spring.annotation.resolver.TextParam;
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

public class YamlJacksonNodeReactiveCustomizer implements JacksonNodeReactiveCustomizer {

    private final MediaType mediaType;

    private final YAMLMapper YAML_MAPPER;

    public YamlJacksonNodeReactiveCustomizer(ObjectMapper objectMapper) {
        this((YAMLMapper) new YAMLMapper().setSerializerFactory(objectMapper.getSerializerFactory()));
    }

    public YamlJacksonNodeReactiveCustomizer(YAMLMapper yamlMapper) {
        this(yamlMapper, MediaType.parseMediaType("application/yaml"));
    }

    public YamlJacksonNodeReactiveCustomizer(YAMLMapper yamlMapper, MediaType mediaType) {
        this.YAML_MAPPER = yamlMapper;
        this.mediaType = mediaType;
    }

    @Override
    public Mono<Void> customize(ObjectNode objectNode, MethodParameter parameter, ServerHttpRequest request) {
        if (!guard(objectNode, parameter, request)) {
            return Mono.empty();
        }
        TextParam parameterAnnotation = parameter.getParameterAnnotation(TextParam.class);
        Assert.state(parameterAnnotation != null, "argument resolver annotation should not be null.");

        return IDataBufferUtils.fluxData2Bytes(request.getBody())
            .map(bytes -> tryDecompressGzip(parameterAnnotation, request, bytes))
            .map(bytes -> {
                Charset charset = Optional.of(request.getHeaders())
                    .map(HttpHeaders::getContentType)
                    .map(MimeType::getCharset)
                    .orElse(StandardCharsets.UTF_8);
                return new String(bytes, charset);
            })
            .doOnNext(jsonStr -> injectBodyParam(objectNode, jsonStr))
            .then();
    }

    protected boolean guard(ObjectNode objectNode, MethodParameter parameter, ServerHttpRequest serverHttpRequest) {
        return mediaType.isCompatibleWith(serverHttpRequest.getHeaders().getContentType());
    }

    protected void injectBodyParam(ObjectNode objectNode, String yamlBody) {
        try {
            JsonNode jsonNode = YAML_MAPPER.readTree(yamlBody);
            if (jsonNode instanceof ObjectNode bodyNode) {
                objectNode.setAll(bodyNode);
            }
        } catch (JsonProcessingException e) {
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
