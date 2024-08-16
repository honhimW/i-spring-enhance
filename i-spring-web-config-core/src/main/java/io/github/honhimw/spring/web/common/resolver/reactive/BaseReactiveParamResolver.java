package io.github.honhimw.spring.web.common.resolver.reactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.spring.data.common.ValidatorUtils;
import io.github.honhimw.spring.util.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2022-07-04
 */

public abstract class BaseReactiveParamResolver implements HandlerMethodArgumentResolver {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final ObjectMapper OBJECT_MAPPER;

    public BaseReactiveParamResolver() {
        this.OBJECT_MAPPER = JsonUtils.getObjectMapper();
    }

    public BaseReactiveParamResolver(ObjectMapper OBJECT_MAPPER) {
        this.OBJECT_MAPPER = OBJECT_MAPPER;
    }

    protected List<JacksonNodeReactiveCustomizer> jacksonNodeCustomizers = new ArrayList<>();

    protected void validate(Object arugment) {
        validate(arugment, null);
    }

    protected void validate(Object arugment, String[] excludesArgs) {
        ValidatorUtils.validate(arugment, excludesArgs);
    }

    protected void assertBaseType(Class<?> parameterType) {
        Assert.isAssignable(Serializable.class, parameterType, "接口参数类型必须为Serializable的子类");
    }

    protected void injectUriParam(ObjectNode objectNode, Map<String, String> uriTemplate) {
        if (MapUtils.isNotEmpty(uriTemplate)) {
            uriTemplate.forEach(objectNode::put);
        }
    }

    protected void injectParameterMap(ObjectNode objectNode, Map<String, List<String>> parameterMap) {
        if (MapUtils.isNotEmpty(parameterMap)) {
            parameterMap.forEach((field, strings) -> {
                if (CollectionUtils.isNotEmpty(strings)) {
                    objectNode.put(field, strings.get(0));
                }
            });
        }
    }

    public void addJacksonNodeCustomizer(JacksonNodeReactiveCustomizer jacksonNodeCustomizer) {
        jacksonNodeCustomizers.add(jacksonNodeCustomizer);
    }

    protected Mono<Void> injectCustom(ObjectNode objectNode, MethodParameter parameter, ServerHttpRequest serverHttpRequest) {
        Mono<Void> mono = Mono.empty();
        if (!jacksonNodeCustomizers.isEmpty()) {
            for (JacksonNodeReactiveCustomizer jacksonNodeCustomizer : jacksonNodeCustomizers) {
                mono = mono.then(jacksonNodeCustomizer.customize(objectNode, parameter, serverHttpRequest));
            }
        }
        return mono;
    }

    protected Object readValue(MethodParameter parameter, ObjectNode node) {
        Object target;
        try {
            Class<?> parameterType = parameter.getParameterType();
            if (parameter.getGenericParameterType() instanceof ParameterizedType parameterizedType) {
                target = OBJECT_MAPPER.readValue(node.traverse(), OBJECT_MAPPER.getTypeFactory().constructType(parameterizedType));
            } else {
                target = OBJECT_MAPPER.readValue(node.traverse(), parameterType);
            }
            if (log.isDebugEnabled()) {
                log.debug(OBJECT_MAPPER.writeValueAsString(node));
            }
            return target;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
