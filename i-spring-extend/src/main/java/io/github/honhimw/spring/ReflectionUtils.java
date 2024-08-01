package io.github.honhimw.spring;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2023-01-17
 */

public class ReflectionUtils {

    public static ParameterizedType getInterface(Object o, Class<?> interfaze) {
        Type type = o.getClass();
        do {
            ParameterizedType anInterface = getInterface(type, interfaze);
            if (Objects.nonNull(anInterface)) {
                return anInterface;
            }
            if (type instanceof Class<?> clz) {
                type = clz.getGenericSuperclass();
            }
        } while (!Objects.equals(type, Object.class));
        return null;
    }

    public static ParameterizedType getInterface(Class<?> clz, Class<?> interfaze) {
        Type[] genericInterfaces = clz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            ParameterizedType anInterface = getInterface(genericInterface, interfaze);
            if (Objects.nonNull(anInterface)) {
                return anInterface;
            }
        }
        return null;
    }


    public static ParameterizedType getInterface(Type type, Class<?> interfaze) {
        if (type instanceof Class<?> clz) {
            Type[] genericInterfaces = clz.getGenericInterfaces();
            for (Type genericInterface : genericInterfaces) {
                ParameterizedType anInterface = getInterface(genericInterface, interfaze);
                if (Objects.nonNull(anInterface)) {
                    return anInterface;
                }
            }
        } else if (type instanceof ParameterizedType parameterizedType) {
            if (Objects.equals(interfaze, parameterizedType.getRawType())) {
                return parameterizedType;
            }
        }
        return null;
    }

}
