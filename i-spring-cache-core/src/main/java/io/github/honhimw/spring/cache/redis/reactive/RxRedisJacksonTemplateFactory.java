package io.github.honhimw.spring.cache.redis.reactive;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.Type;

/**
 * @author hon_him
 * @since 2023-06-26
 */
@SuppressWarnings({"unused"})
public interface RxRedisJacksonTemplateFactory {

    ObjectMapper getMapper();

    RedisSerializer<String> keySerializer();

    ReactiveRedisTemplate<String, String> string();

    <V> ReactiveRedisTemplate<String, V> forType(@Nonnull Type type);

    <V> ReactiveRedisTemplate<String, V> forType(@Nonnull Class<V> type);

    <V> ReactiveRedisTemplate<String, V> forType(@Nonnull JavaType type);

    @SuppressWarnings("unchecked")
    default <V> ReactiveRedisTemplate<String, V> forType(@Nonnull V value) {
        return forType((Class<V>) value.getClass());
    }

}
