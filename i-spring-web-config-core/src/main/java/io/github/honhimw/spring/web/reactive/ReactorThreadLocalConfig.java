package io.github.honhimw.spring.web.reactive;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ThreadLocalAccessor;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Hooks;
import reactor.util.context.Context;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author hon_him
 * @since 2024-07-25
 */

@Slf4j
public class ReactorThreadLocalConfig {

    private static final ReactorThreadLocalConfig INSTANCE = new ReactorThreadLocalConfig();

    private ReactorThreadLocalConfig() {
    }
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private final Map<String, Accessor<?>> accessorMap = new ConcurrentHashMap<>();

    public static void initialize() {
        INSTANCE._initialize();
    }

    public static <T> BiFunction<Context, T, Context> addAccessor(String key, Predicate<T> guard, Supplier<T> getter, Consumer<T> setter, Runnable cleaner) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(guard, "guard must not be null");
        Objects.requireNonNull(getter, "getter must not be null");
        Objects.requireNonNull(setter, "setter must not be null");
        Objects.requireNonNull(cleaner, "cleaner must not be null");
        INSTANCE.accessorMap.put(key, new Accessor<>(guard, getter, setter, cleaner));

        return  (context, value) -> context.put(key, value);
    }

    private void _initialize() {
        if (!initialized.compareAndSet(false, true)) {
            log.warn("thread-local config has been initialized");
            return;
        }

        accessorMap.forEach((_key, accessor) -> {
            log.info("register thread-local accessor: [{}]", _key);
            ContextRegistry.getInstance()
                .registerThreadLocalAccessor(buildThreadLocalAccessor(_key, accessor));
        });
        Hooks.enableAutomaticContextPropagation();
    }

    private <T> ThreadLocalAccessor<T> buildThreadLocalAccessor(String key, Accessor<T> accessor) {
        return new ThreadLocalAccessor<>() {
            @Nonnull
            public Object key() {
                return key;
            }

            @Nullable
            public T getValue() {
                if (accessor.check()) {
                    return accessor.get();
                }
                return null;
            }

            public void setValue(@Nonnull T value) {
                accessor.set(value);
            }

            public void setValue() {
                accessor.cleaner().run();
            }
        };
    }

    private record Accessor<T>(Predicate<T> guard, Supplier<T> getter, Consumer<T> setter, Runnable cleaner) {

        private boolean check() {
            return guard.test(get());
        }

        private void set(T t) {
            setter.accept(t);
        }

        private T get() {
            return getter.get();
        }


        private void clean() {
            cleaner.run();
        }
    }

}
