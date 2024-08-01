package io.github.honhimw.spring.web.common;

import io.github.honhimw.spring.Result;
import jakarta.annotation.Nonnull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 仅支持一种异常
 * @author hon_him
 * @since 2023-05-09
 */
@SuppressWarnings({"unchecked", "unused"})
public abstract class SingleExceptionWrapper<E extends Throwable> implements ExceptionWrapper {

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

    /**
     * make result code/error coed/http code return the very same value
     * @param e exception to be wrapped
     * @return unify code
     */
    protected int unifyCode(@Nonnull E e) {
        return httpCode();
    }

    @Nonnull
    @Override
    public final Result<Void> wrap(@Nonnull Throwable e) {
        E e1 = (E) e;
        String msg = _wrap(e1);
        Result<Void> result = Result.empty();
        result.msg(msg);
        result.code(_resultCode(e1));
        return result;
    }

    @Nonnull
    protected abstract String _wrap(@Nonnull E e);

    @Override
    public final String resultCode(Throwable e) {
        return _resultCode((E) e);
    }

    protected String _resultCode(@Nonnull E e) {
        return String.valueOf(unifyCode(e));
    }

    @Override
    public final int httpCode(Throwable e) {
        return _httpCode((E) e);
    }

    protected int _httpCode(@Nonnull E e) {
        return unifyCode(e);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 1;
    }
}
