package io.github.honhimw.spring.web.common.resolver.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.spring.IDataBufferUtils;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.util.GZipUtils;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author hon_him
 * @since 2022-10-19
 */

public class TextReactiveParamResolver extends BaseReactiveParamResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TextParam.class);
    }

    @Nonnull
    @Override
    public Mono<Object> resolveArgument(@Nonnull MethodParameter parameter, @Nonnull BindingContext bindingContext,
        ServerWebExchange exchange) {
        TextParam parameterAnnotation = parameter.getParameterAnnotation(TextParam.class);
        Assert.notNull(parameterAnnotation, "argument resolver annotation should not be null.");

        ServerHttpRequest request = exchange.getRequest();
        Map<String, String> uriTemplateVars = exchange.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        return Mono.fromSupplier(OBJECT_MAPPER::createObjectNode)
            .doOnNext(objectNode -> injectParameterMap(objectNode, queryParams))
            .doOnNext(objectNode -> injectUriParam(objectNode, uriTemplateVars))
            .flatMap(objectNode -> exchange.getFormData()
                .doOnNext(formData -> injectParameterMap(objectNode, formData))
                .thenReturn(objectNode))
            .flatMap(objectNode -> {
                if (MediaType.APPLICATION_JSON.isCompatibleWith(request.getHeaders().getContentType())) {
                    return IDataBufferUtils.fluxData2Bytes(request.getBody())
                        .map(bytes -> tryDecompressGzip(parameterAnnotation, request, bytes))
                        .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                        .doOnNext(jsonStr -> injectBodyParam(objectNode, jsonStr))
                        .thenReturn(objectNode);
                } else {
                    return Mono.just(objectNode);
                }
            })
            .flatMap(objectNode -> injectCustom(objectNode, parameter, request).thenReturn(objectNode))
            .map(objectNode -> readValue(parameter, objectNode))
            .doOnNext(target -> validate(target, parameterAnnotation.excludesValidate()));
    }

    protected void injectBodyParam(ObjectNode objectNode, String jsonBody) {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonBody);
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
