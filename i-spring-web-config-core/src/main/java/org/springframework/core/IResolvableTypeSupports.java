package org.springframework.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author hon_him
 * @since 2024-12-03
 */

public class IResolvableTypeSupports {

    public static Object readValue(MethodParameter parameter, ObjectNode node, ObjectMapper mapper) {
        try {
            Class<?> parameterType = parameter.getParameterType();
            if (CharSequence.class.isAssignableFrom(parameterType)) {
                return node.toString();
            }
            ResolvableType resolvableType = IResolvableTypeSupports.resolve(parameter);
            Type type = resolvableType.getType();
            return mapper.treeToValue(node, mapper.getTypeFactory().constructType(type));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static ResolvableType resolve(MethodParameter methodParameter) {
        ResolvableType resolvableType = ResolvableType.forMethodParameter(methodParameter);
        ResolvableType resolveType = resolvableType.resolveType();
        if (resolveType == ResolvableType.NONE) {
            return resolvableType;
        } else {
            return resolveType;
        }
    }

    public static ResolvableType resolveType(ResolvableType resolvableType) {
        return resolvableType.resolveType();
    }

}
