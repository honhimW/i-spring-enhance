package io.github.honhimw.spring.web.common.resolver.reactive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.spring.IDataBufferUtils;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.spring.ResolvableTypes;
import io.github.honhimw.util.GZipUtils;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.IResolvableTypeSupports;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.PayloadTooLargeException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

/**
 * @author hon_him
 * @since 2022-10-19
 */

public class TextReactiveParamResolver extends BaseReactiveParamResolver {

    private final AbstractJackson2Decoder jackson2Decoder;

    public TextReactiveParamResolver(AbstractJackson2Decoder jackson2Decoder) {
        this.jackson2Decoder = jackson2Decoder;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TextParam.class);
    }

    @Nonnull
    @Override
    public Mono<Object> resolveArgument(@Nonnull MethodParameter parameter,
                                        @Nonnull BindingContext bindingContext,
                                        ServerWebExchange exchange) {
        TextParam parameterAnnotation = parameter.getParameterAnnotation(TextParam.class);
        Assert.notNull(parameterAnnotation, "argument resolver annotation should not be null.");

        ServerHttpRequest request = exchange.getRequest();
        Map<String, String> uriTemplateVars = exchange.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        MultiValueMap<String, String> queryParams = request.getQueryParams();

        ResolvableType bodyType = ResolvableType.forMethodParameter(parameter);
        Class<?> resolvedType = bodyType.resolve();
        ReactiveAdapter adapter = (resolvedType != null ? getAdapterRegistry().getAdapter(resolvedType) : null);
        ResolvableType elementType = (adapter != null ? bodyType.getGeneric() : bodyType);

        return Mono.fromSupplier(jackson2Decoder.getObjectMapper()::createObjectNode)
            .doOnNext(objectNode -> injectParameterMap(objectNode, queryParams))
            .doOnNext(objectNode -> injectUriParam(objectNode, uriTemplateVars))
            .flatMap(objectNode -> exchange.getFormData()
                .doOnNext(formData -> injectParameterMap(objectNode, formData))
                .thenReturn(objectNode))
            .flatMap(objectNode -> readBody(parameterAnnotation, parameter, adapter, elementType, request, objectNode))
            .doOnNext(target -> validate(target, parameterAnnotation.excludesValidate()));
    }

    protected Mono<Object> readBody(
        TextParam parameterAnnotation,
        MethodParameter parameter,
        ReactiveAdapter adapter,
        ResolvableType elementType,
        ServerHttpRequest request,
        ObjectNode objectNode) {
        if (MediaType.APPLICATION_JSON.isCompatibleWith(request.getHeaders().getContentType())) {
            Flux<DataBuffer> body = request.getBody();
            String contentEncoding = request.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
            if (parameterAnnotation.gzip() && StringUtils.equals(contentEncoding, "gzip")) {
                body = body.handle((dataBuffer, sink) -> {
                    byte[] bytes = IDataBufferUtils.dataBuffer2Bytes(dataBuffer);
                    try {
                        byte[] decompress = GZipUtils.decompress(bytes);
                        sink.next(DefaultDataBufferFactory.sharedInstance.wrap(decompress));
                    } catch (IOException e) {
                        sink.error(new IllegalArgumentException(e));
                    }
                });
            }

            if (adapter != null && adapter.isMultiValue()) {
                Flux<?> flux = jackson2Decoder.decode(body, ResolvableTypes.OBJECT_NODE_TYPE, null, null)
                    .cast(ObjectNode.class)
                    .doOnNext(objectNode::setAll)
                    .flatMap(node -> injectCustom(node, parameter, request).thenReturn(objectNode))
                    .map(node -> IResolvableTypeSupports.readValue(elementType, node, jackson2Decoder.getObjectMapper()))
                    .onErrorMap(ex -> handleReadError(parameter, ex));
                return Mono.just(adapter.fromPublisher(flux));
            } else {
                Mono<?> mono = jackson2Decoder.decodeToMono(body, ResolvableTypes.OBJECT_NODE_TYPE, null, null)
                    .cast(ObjectNode.class)
                    .doOnNext(objectNode::setAll)
                    .flatMap(node -> injectCustom(node, parameter, request))
                    .thenReturn(objectNode)
                    .map(node -> IResolvableTypeSupports.readValue(elementType, node, jackson2Decoder.getObjectMapper()))
                    .onErrorMap(ex -> handleReadError(parameter, ex));
                return (adapter != null ? Mono.just(adapter.fromPublisher(mono)) : Mono.from(mono));
            }
        }
        Mono<?> mono = Mono.just(objectNode)
            .map(node -> IResolvableTypeSupports.readValue(elementType, node, jackson2Decoder.getObjectMapper()))
            .onErrorMap(ex -> handleReadError(parameter, ex));
        return (adapter != null ? Mono.just(adapter.fromPublisher(mono)) : Mono.from(mono));
    }

    protected void injectBodyParam(ObjectNode objectNode, byte[] jsonBody) {
        try {
            JsonNode jsonNode = jackson2Decoder.getObjectMapper().readTree(jsonBody);
            if (jsonNode instanceof ObjectNode bodyNode) {
                objectNode.setAll(bodyNode);
            }
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

    protected Throwable handleReadError(MethodParameter parameter, Throwable ex) {
        if (ex instanceof DataBufferLimitException) {
            return new PayloadTooLargeException(ex);
        }
        if (ex instanceof DecodingException) {
            return new ServerWebInputException("Failed to read HTTP message", parameter, ex);
        }
        return ex;
    }

}
