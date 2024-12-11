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

    private static final ResolvableType CHAR_SEQUENCE_TYPE = ResolvableType.forType(CharSequence.class);

    public static Object readValue(MethodParameter parameter, ObjectNode node, ObjectMapper mapper) {
        try {
            Class<?> parameterType = parameter.getParameterType();
            if (CharSequence.class.isAssignableFrom(parameterType)) {
                return node.toString();
            }

            ResolvableType bodyType = ResolvableType.forMethodParameter(parameter);
            Class<?> resolvedType = bodyType.resolve();
            ReactiveAdapter adapter = (resolvedType != null ? ReactiveAdapterRegistry.getSharedInstance().getAdapter(resolvedType) : null);
            ResolvableType elementType = (adapter != null ? bodyType.getGeneric() : bodyType);
            ResolvableType resolvableType = resolve(elementType);
            Type type = resolvableType.getType();
            return mapper.treeToValue(node, mapper.getTypeFactory().constructType(type));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Object readValue(ResolvableType elementType, ObjectNode node, ObjectMapper mapper) {
        try {
            if (CHAR_SEQUENCE_TYPE.isAssignableFrom(elementType)) {
                return mapper.writeValueAsString(node);
            }
            ResolvableType resolvableType = resolve(elementType);
            Type type = resolvableType.getType();
            return mapper.treeToValue(node, mapper.getTypeFactory().constructType(type));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static ResolvableType resolve(MethodParameter methodParameter) {
        ResolvableType resolvableType = ResolvableType.forMethodParameter(methodParameter);
        return resolve(resolvableType);
    }

    public static ResolvableType resolve(ResolvableType resolvableType) {
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
