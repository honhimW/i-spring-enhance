package io.github.honhimw.spring.web.common.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.spring.util.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.*;
import jakarta.validation.bootstrap.GenericBootstrap;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.validation.beanvalidation.LocaleContextMessageInterpolator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hon_him
 * @since 2022-07-04
 */

public abstract class BaseParamResolver implements HandlerMethodArgumentResolver {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected static final ObjectMapper OBJECT_MAPPER = JsonUtils.getObjectMapper();

    protected static final Validator validator;

    static {
        GenericBootstrap genericBootstrap = Validation.byDefaultProvider();
        Configuration<?> configure = genericBootstrap.configure();
        MessageInterpolator defaultMessageInterpolator = configure.getDefaultMessageInterpolator();
        configure.messageInterpolator(new LocaleContextMessageInterpolator(defaultMessageInterpolator));
        ValidatorFactory validatorFactory = configure.buildValidatorFactory();
        validator = validatorFactory.getValidator();
        validatorFactory.close();
    }

    private static final Class<?>[] GROUPS = new Class[0];

    protected List<JacksonNodeCustomizer> jacksonNodeCustomizers = new ArrayList<>();

    protected void validate(Object arugment) {
        validate(arugment, null);
    }

    protected void validate(Object arugment, String[] excludesArgs) {
        Set<ConstraintViolation<Object>> validResult = validator.validate(arugment, GROUPS);
        if (StringUtils.isNoneBlank(excludesArgs)) {
            Set<String> ea = Arrays.stream(excludesArgs).collect(Collectors.toSet());
            validResult = validResult.stream().filter(cv -> !ea.contains(cv.getPropertyPath().toString())).collect(Collectors.toSet());
        }
        if (!validResult.isEmpty()) {
            throw new ConstraintViolationException(validResult);
        }
    }

    protected void assertBaseType(Class<?> parameterType) {
        Assert.isAssignable(Serializable.class, parameterType, "接口参数类型实现Serializable接口");
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

    protected Object readValue(MethodParameter parameter, ObjectNode node) throws Exception {
        Object target;
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
    }

}
