package io.github.honhimw.spring.web.common.resolver;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.spring.ValidatorUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2022-07-04
 */

public abstract class BaseParamResolver implements HandlerMethodArgumentResolver {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected BaseParamResolver() {
    }

    protected List<JacksonNodeCustomizer> jacksonNodeCustomizers = new ArrayList<>();

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

    protected void injectParameterMap(ObjectNode objectNode, Map<String, String[]> parameterMap) {
        if (MapUtils.isNotEmpty(parameterMap)) {
            parameterMap.forEach((field, strings) -> {
                if (ArrayUtils.isNotEmpty(strings)) {
                    objectNode.put(field, strings[0]);
                }
            });
        }
    }

    public void addJacksonNodeCustomizer(JacksonNodeCustomizer jacksonNodeCustomizer) {
        jacksonNodeCustomizers.add(jacksonNodeCustomizer);
    }

    protected void injectCustom(ObjectNode objectNode, MethodParameter parameter, HttpServletRequest httpServletRequest) {
        if (!jacksonNodeCustomizers.isEmpty()) {
            for (JacksonNodeCustomizer jacksonNodeCustomizer : jacksonNodeCustomizers) {
                jacksonNodeCustomizer.customize(objectNode, parameter, httpServletRequest);
            }
        }
    }

}
