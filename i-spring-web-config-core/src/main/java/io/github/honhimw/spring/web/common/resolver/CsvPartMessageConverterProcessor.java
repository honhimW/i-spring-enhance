package io.github.honhimw.spring.web.common.resolver;

import io.github.honhimw.spring.web.mvc.AbstractFileMessageConverterProcessor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.Collection;
import java.util.List;

/**
 * @author hon_him
 * @since 2024-08-08
 */

public class CsvPartMessageConverterProcessor extends AbstractFileMessageConverterProcessor {

    public CsvPartMessageConverterProcessor(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return super.supportsParameter(parameter) &&
               Collection.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public boolean supportsReturnType(@NonNull MethodParameter returnType) {
        return super.supportsReturnType(returnType) &&
               Collection.class.isAssignableFrom(returnType.getParameterType());
    }

}
