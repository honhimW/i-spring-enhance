package io.github.honhimw.spring.web.common;

import jakarta.annotation.Nonnull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Single kind of exception wrapper
 *
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
    public final boolean support(@Nonnull Throwable e) {
        return type().isAssignableFrom(e.getClass()) && _support((E) e);
    }

    protected boolean _support(@Nonnull E e) {
        return true;
    }

    @Nonnull
    @Override
    public final String wrap(@Nonnull Throwable e) {
        return _wrap((E) e);
    }

    @Nonnull
    protected abstract String _wrap(@Nonnull E e);

    @Override
    public final int httpCode(Throwable e) {
        return _httpCode((E) e);
    }

    protected abstract int _httpCode(@Nonnull E e);

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 1000;
    }
}
