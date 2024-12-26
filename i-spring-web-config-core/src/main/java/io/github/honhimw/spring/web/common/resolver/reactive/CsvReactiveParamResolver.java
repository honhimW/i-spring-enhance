package io.github.honhimw.spring.web.common.resolver.reactive;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.github.honhimw.core.WrappedException;
import io.github.honhimw.spring.IDataBufferUtils;
import io.github.honhimw.spring.ValidatorUtils;
import io.github.honhimw.spring.annotation.resolver.PartParam;
import io.github.honhimw.util.GZipUtils;
import jakarta.annotation.Nonnull;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolverSupport;
import org.springframework.web.server.MissingRequestValueException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * @author hon_him
 * @since 2024-08-05
 */

public class CsvReactiveParamResolver extends HandlerMethodArgumentResolverSupport implements HandlerMethodArgumentResolver {

    private final CsvMapper CSV_MAPPER;

    private final CsvSchema CSV_SCHEMA;

    public CsvReactiveParamResolver(ObjectMapper objectMapper) {
        this((CsvMapper) new CsvMapper().setSerializerFactory(objectMapper.getSerializerFactory()));
    }

    public CsvReactiveParamResolver(CsvMapper csvMapper) {
        super(ReactiveAdapterRegistry.getSharedInstance());
        csvMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.CSV_MAPPER = csvMapper;
        this.CSV_SCHEMA = csvMapper.schemaWithHeader();
    }

    @Override
    public boolean supportsParameter(@Nonnull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PartParam.class) &&
               checkParameterType(parameter, Collection.class::isAssignableFrom);
    }

    @Nonnull
    @Override
    public Mono<Object> resolveArgument(@Nonnull MethodParameter parameter,
                                        @Nonnull BindingContext bindingContext,
                                        @Nonnull ServerWebExchange exchange) {
        PartParam partParam = parameter.getParameterAnnotation(PartParam.class);
        Assert.notNull(partParam, "argument resolver annotation should not be null.");

        ServerHttpRequest request = exchange.getRequest();
        MediaType contentType = request.getHeaders().getContentType();

        String name = partParam.value();
        boolean required = partParam.required();

        ReactiveAdapter adapter = getAdapterRegistry().getAdapter(parameter.getParameterType());
        Class<?> parameterType;
        Type genericParameterType;
        if (adapter == null) {
            parameterType = parameter.getParameterType();
            genericParameterType = parameter.getGenericParameterType();
        } else {
            parameterType = parameter.nested().getNestedParameterType();
            genericParameterType = parameter.nested().getNestedGenericParameterType();
        }

        if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
            Mono<Part> requestPart = exchange.getMultipartData().handle((multiValueMap, sink) -> {
                Part part = multiValueMap.getFirst(name);
                if (part == null) {
                    if (required) {
                        sink.error(new MissingRequestValueException(name, parameterType, "request part", parameter));
                    }
                } else {
                    sink.next(part);
                }
            });

            Mono<Object> publisher = requestPart.flatMap(part -> DataBufferUtils.join(part.content()))
                .map(IDataBufferUtils::dataBuffer2Bytes)
                .<String>handle((bytes, sink) -> {
                    if (partParam.gzip()) {
                        try {
                            bytes = GZipUtils.decompress(bytes);
                        } catch (IOException e) {
                            sink.error(new RuntimeException(e));
                            return;
                        }
                    }
                    Charset charset;
                    if (bytes.length > 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                        charset = StandardCharsets.UTF_8;
                    } else {
                        charset = Charset.forName("GBK");
                    }
                    sink.next(new String(bytes, charset));
                })
                .handle((csvBody, sink) -> {
                    JavaType javaType = CSV_MAPPER.constructType(genericParameterType);
                    ArrayNode arrayNode = CSV_MAPPER.createArrayNode();
                    try (MappingIterator<ObjectNode> iterator = CSV_MAPPER.readerFor(ObjectNode.class).with(CSV_SCHEMA).readValues(csvBody)) {
                        iterator.forEachRemaining(arrayNode::add);
                        Collection<?> arguments = CSV_MAPPER.readValue(arrayNode.traverse(), javaType);
                        for (Object argument : arguments) {
                            ValidatorUtils.validate(argument, partParam.excludesValidate());
                        }
                        sink.next(arguments);
                    } catch (Exception e) {
                        sink.error(new WrappedException(e));
                    }
                });
            if (adapter != null) {
                return Mono.just(adapter.fromPublisher(publisher));
            } else {
                return publisher;
            }
        }

        if (required) {
            return Mono.error(new MissingRequestValueException(name, parameterType, "request part", parameter));
        } else {
            return Mono.empty();
        }

    }
}
