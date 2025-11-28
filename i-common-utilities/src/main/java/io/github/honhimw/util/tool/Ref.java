package io.github.honhimw.util.tool;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * @author honhimW
 * @since 2025-10-13
 */

public class Ref<T> implements Supplier<T> {

    @Nullable
    private volatile T value;

    @Nullable
    private final Supplier<@NonNull T> supplier;

    private Ref(@Nullable T value, @Nullable Supplier<@NonNull T> supplier) {
        this.value = value;
        this.supplier = supplier;
    }

    /**
     * Lazy load value
     *
     * @param supplier value supplier, will be call only once
     * @param <T>      any type
     * @return new instance
     */
    public static <T> Ref<T> of(@NonNull Supplier<@NonNull T> supplier) {
        return new Ref<>(null, supplier);
    }

    /**
     * Eager load value
     *
     * @param value value
     * @param <T>   any type
     * @return new instance
     */
    public static <T> Ref<T> of(@NonNull T value) {
        return new Ref<>(value, null);
    }

    @Override
    public T get() {
        if (value == null && supplier != null) {
            synchronized (this) {
                if (value == null) {
                    value = supplier.get();
                }
            }
        }
        return value;
    }

}
