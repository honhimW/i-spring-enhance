package io.github.honhimw.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2023-05-19
 */

@SuppressWarnings({"unused", "unchecked"})
public class SimpleContextHolder {

    private static final ThreadLocal<Map<String, Object>> _THREAD_LOCAL = new InheritableThreadLocal<>();

    public static Optional<Map<String, Object>> copyOptionMap() {
        return Optional.of(_THREAD_LOCAL)
            .map(ThreadLocal::get)
            .map(HashMap::new);
    }
    public static Map<String, Object> copyMap() {
        return copyOptionMap()
            .orElse(null);
    }

    public static <R> Optional<R> option(String key) {
        return Optional.of(_THREAD_LOCAL)
            .map(ThreadLocal::get)
            .map(map -> map.get(key))
            .map(o -> (R) o);
    }
    public static <R> R get(String key) {
        return (R) option(key).orElse(null);
    }

    public static void put(String key, Object value) {
        Map<String, Object> map = _THREAD_LOCAL.get();
        if (Objects.isNull(map)) {
            map = new HashMap<>();
            _THREAD_LOCAL.set(map);
        }
        map.put(key, value);
    }

    public static void put(Map<String, Object> kvs) {
        Map<String, Object> map = _THREAD_LOCAL.get();
        if (Objects.isNull(map)) {
            map = new HashMap<>();
            _THREAD_LOCAL.set(map);
        }
        map.putAll(kvs);
    }

    public static void clear() {
        Map<String, Object> map = _THREAD_LOCAL.get();
        if (Objects.nonNull(map)) {
            map.clear();
            _THREAD_LOCAL.remove();
        }
    }
}
