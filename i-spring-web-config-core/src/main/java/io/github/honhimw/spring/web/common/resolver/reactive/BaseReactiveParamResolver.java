package io.github.honhimw.spring.web.common.resolver.reactive;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.spring.ValidatorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolverSupport;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2022-07-04
 */

public abstract class BaseReactiveParamResolver extends HandlerMethodArgumentResolverSupport implements HandlerMethodArgumentResolver {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    public BaseReactiveParamResolver() {
        super(ReactiveAdapterRegistry.getSharedInstance());
    }

    protected List<JacksonNodeReactiveCustomizer> jacksonNodeCustomizers = new ArrayList<>();

    protected void validate(Object arugment) {
        validate(arugment, null);
    }

    protected void validate(Object arugment, String[] excludesArgs) {
        ValidatorUtils.validate(arugment, excludesArgs);
    }

    protected void assertBaseType(Class<?> parameterType) {
        Assert.isAssignable(Serializable.class, parameterType, "ParameterType must be Serializable");
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

}
