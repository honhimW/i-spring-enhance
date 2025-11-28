package io.github.honhimw.spring.cache.redis;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.Type;

/**
 * @author hon_him
 * @since 2023-06-26
 */
@SuppressWarnings({"unused"})
public interface RedisJacksonTemplateFactory {

    ObjectMapper getMapper();

    RedisSerializer<String> keySerializer();

    RedisTemplate<String, String> string();

    RedisTemplate<String, byte[]> bytes();

    <V> RedisTemplate<String, V> forType(@NonNull Type type);

    <V> RedisTemplate<String, V> forType(@NonNull Class<V> type);

    <V> RedisTemplate<String, V> forType(@NonNull JavaType type);

    @SuppressWarnings("unchecked")
    default <V> RedisTemplate<String, V> forType(@NonNull V value) {
        return forType((Class<V>) value.getClass());
    }

}
