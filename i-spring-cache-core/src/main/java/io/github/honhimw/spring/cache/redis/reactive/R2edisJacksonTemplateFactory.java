package io.github.honhimw.spring.cache.redis.reactive;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.Type;

/**
 * @author hon_him
 * @since 2023-06-26
 */
@SuppressWarnings({"unused"})
public interface R2edisJacksonTemplateFactory {

    ObjectMapper getMapper();

    RedisSerializer<String> keySerializer();

    ReactiveRedisTemplate<String, String> string();

    ReactiveRedisTemplate<String, byte[]> bytes();

    <V> ReactiveRedisTemplate<String, V> forType(@NonNull Type type);

    <V> ReactiveRedisTemplate<String, V> forType(@NonNull Class<V> type);

    <V> ReactiveRedisTemplate<String, V> forType(@NonNull JavaType type);

    @SuppressWarnings("unchecked")
    default <V> ReactiveRedisTemplate<String, V> forType(@NonNull V value) {
        return forType((Class<V>) value.getClass());
    }

}
