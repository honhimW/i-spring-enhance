package io.github.honhimw.spring.web.common.resolver;

import io.github.honhimw.spring.web.mvc.AbstractFileMessageConverterProcessor;
import jakarta.annotation.Nonnull;
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
    public boolean supportsParameter(@Nonnull MethodParameter parameter) {
        return super.supportsParameter(parameter) &&
               Collection.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public boolean supportsReturnType(@Nonnull MethodParameter returnType) {
        return super.supportsReturnType(returnType) &&
               Collection.class.isAssignableFrom(returnType.getParameterType());
    }

}
