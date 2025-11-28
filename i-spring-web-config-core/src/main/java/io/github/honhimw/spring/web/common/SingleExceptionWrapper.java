package io.github.honhimw.spring.web.common;

import org.jspecify.annotations.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Single kind of exception wrapper
 * @author hon_him
 * @since 2023-05-09
 */
@SuppressWarnings({"unchecked", "unused"})
public abstract class SingleExceptionWrapper<E extends Throwable> implements ExceptionWrapper.MessageExceptionWrapper {

    private Class<E> genericType() {
        ParameterizedType parameterizedType =
            (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class<E>) actualTypeArguments[0];
    }

    protected final Class<E> _type = genericType();

    public Class<E> type() {
        return this._type;
    }

    @Override
    public final boolean support(@NonNull Throwable e) {
        return type().isAssignableFrom(e.getClass()) && _support((E) e);
    }

    protected boolean _support(@NonNull E e) {
        return true;
    }

    @NonNull
    @Override
    public final String wrap(@NonNull Throwable e) {
        return _wrap((E) e);
    }

    @NonNull
    protected abstract String _wrap(@NonNull E e);

    @Override
    public final int httpCode(Throwable e) {
        return _httpCode((E) e);
    }

    protected abstract int _httpCode(@NonNull E e);

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 1000;
    }
}
