package io.github.honhimw.spring.cache;

import jakarta.annotation.Nullable;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ttl < 0表示不会超时
 * @author hon_him
 * @since 2023-06-28
 */

public interface TTLCache<K, V> extends Map<K, V> {

    void put(K key, V value, Duration ttl);

    void expire(K key, Duration ttl);

    void discard();

    @Nullable
    Long getExpire(K key, TimeUnit unit);

    @Nullable
    default Long getExpire(K key) {
        return getExpire(key, TimeUnit.SECONDS);
    }

}
