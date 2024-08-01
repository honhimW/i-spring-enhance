package io.github.honhimw.spring.web.util;

import jakarta.annotation.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

import java.util.function.Consumer;

/**
 * @author hon_him
 * @since 2023-07-20
 */

@SuppressWarnings({"all", "unchecked"})
public class EmptyObjectProvider<T> implements ObjectProvider<T> {

    public static final EmptyObjectProvider INSTANCE = new EmptyObjectProvider<>();

    public static <T> EmptyObjectProvider<T> getInstance() {
        return (EmptyObjectProvider<T>) INSTANCE;
    }

    @Override
    public T getObject(@Nullable Object... args) throws BeansException {
        return null;
    }

    @Override
    public T getIfAvailable() throws BeansException {
        return null;
    }

    @Override
    public T getIfUnique() throws BeansException {
        return null;
    }

    @Override
    public T getObject() throws BeansException {
        return null;
    }

    @Override
    public void forEach(Consumer action) {
        // do nothing
    }

}
