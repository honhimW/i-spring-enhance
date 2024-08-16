package io.github.honhimw.spring.web.common.resolver.reactive;

import io.github.honhimw.spring.web.reactive.AbstractReactiveFileResultHandler;
import io.github.honhimw.spring.web.util.MimeTypeSupports;
import jakarta.annotation.Nonnull;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapter;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.FixedContentTypeResolver;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author hon_him
 * @since 2024-08-09
 */

public class CsvReactiveFileResultHandler extends AbstractReactiveFileResultHandler {

    public CsvReactiveFileResultHandler(List<HttpMessageWriter<?>> messageWriters) {
        super(messageWriters, new FixedContentTypeResolver(MimeTypeSupports.TEXT_CSV));
        setOrder(1);
    }

    @Override
    public boolean supports(@Nonnull HandlerResult result) {
        return super.supports(result) &&
               checkParameterType(result, Collection.class::isAssignableFrom);
    }

    protected boolean checkParameterType(HandlerResult result, Predicate<Class<?>> predicate) {
        MethodParameter parameter = result.getReturnTypeSource();
        Class<?> type = parameter.getParameterType();
        ReactiveAdapter adapter = getAdapter(result);
        if (adapter != null) {
            assertHasValues(adapter, parameter);
            type = parameter.nested().getNestedParameterType();
        }
        return predicate.test(type);
    }

    private void assertHasValues(ReactiveAdapter adapter, MethodParameter param) {
        if (adapter.isNoValue()) {
            throw new IllegalArgumentException(
                "No value reactive types not supported: " + param.getGenericParameterType());
        }
    }

}
