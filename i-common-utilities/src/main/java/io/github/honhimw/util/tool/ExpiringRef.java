package io.github.honhimw.util.tool;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author honhimW
 * @since 2025-11-18
 */

public class ExpiringRef<T> implements Supplier<T> {

    @Nullable
    private volatile T value;

    private volatile LocalDateTime updatedAt;

    private volatile LocalDateTime expiredAt;

    private final Consumer<@NonNull Cfg<T>> configurer;

    public static <T> ExpiringRef<T> of(Consumer<@NonNull Cfg<T>> supplier) {
        return new ExpiringRef<>(supplier);
    }

    private ExpiringRef(Consumer<@NonNull Cfg<T>> configurer) {
        this.expiredAt = LocalDateTime.MIN;
        this.configurer = configurer;
    }

    @Override
    public T get() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiredAt)) {
            synchronized (this) {
                if (now.isAfter(expiredAt)) {
                    refresh();
                }
            }
        }
        return value;
    }

    public void refresh() {
        LocalDateTime now = LocalDateTime.now();
        Cfg<T> cfg = new Cfg<>();
        configurer.accept(cfg);
        this.value = cfg.value;
        this.expiredAt = cfg.expiredAt != null
            ? cfg.expiredAt
            : cfg.ttl != null ? now.plus(cfg.ttl) : LocalDateTime.MIN;
        this.updatedAt = now;
    }

    public LocalDateTime expiredAt() {
        return expiredAt;
    }

    public LocalDateTime updatedAt() {
        return updatedAt;
    }

    public Duration elapsed() {
        return Duration.between(expiredAt, LocalDateTime.now());
    }

    public static class Cfg<T> {
        private T value;
        private LocalDateTime expiredAt;
        private Duration ttl;

        private Cfg() {
        }

        public Cfg<T> value(T value) {
            this.value = value;
            return this;
        }

        public Cfg<T> expiredAt(LocalDateTime expiredAt) {
            this.expiredAt = expiredAt;
            return this;
        }

        public Cfg<T> ttl(Duration ttl) {
            this.ttl = ttl;
            return this;
        }
    }

}
