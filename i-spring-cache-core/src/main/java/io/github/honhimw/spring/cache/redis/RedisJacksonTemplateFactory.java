package io.github.honhimw.spring.cache.redis;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
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

    <V> RedisTemplate<String, V> forType(@Nonnull Type type);

    <V> RedisTemplate<String, V> forType(@Nonnull Class<V> type);

    <V> RedisTemplate<String, V> forType(@Nonnull JavaType type);

    @SuppressWarnings("unchecked")
    default <V> RedisTemplate<String, V> forType(@Nonnull V value) {
        return forType((Class<V>) value.getClass());
    }

}
