package io.github.honhimw.spring.cache.memory;

import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hon_him
 * @since 2023-06-28
 */

public class CacheContext {

    /**
     * Refresh Cache event
     */
    public static final String EVENT = "event";

    private final Map<String, Object> map = new ConcurrentHashMap<>();

    public CacheContext() {
    }

    public CacheContext(ApplicationEvent event) {
        put(EVENT, event);
    }

    public void put(String key, Object value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
        } else {
            throw new IllegalArgumentException(String.format("key: %s already exists.", key));
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <V> V get(String key) {
        return (V) map.get(key);
    }

    public void close() {
        map.clear();
    }

}
