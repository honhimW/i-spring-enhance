package io.github.honhimw.spring.cache.redis.reactive;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hon_him
 * @since 2022-06-16
 */
@Slf4j
@SuppressWarnings({"unused", "unchecked"})
public class R2edisJacksonTemplateFactoryImpl implements R2edisJacksonTemplateFactory {

    private final ReactiveRedisConnectionFactory redisConnectionFactory;

    private final RedisSerializer<String> keySerializer;

    private final ObjectMapper REDIS_OBJECT_MAPPER;

    private final Map<JavaType, ReactiveRedisTemplate<String, ?>> redisTemplateMap = new ConcurrentHashMap<>();

    private volatile ReactiveStringRedisTemplate stringReactiveRedisTemplate;

    public R2edisJacksonTemplateFactoryImpl(ReactiveRedisConnectionFactory redisConnectionFactory, RedisSerializer<String> keySerializer, ObjectMapper objectMapper) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.keySerializer = keySerializer;
        this.REDIS_OBJECT_MAPPER = objectMapper;
    }

    @Override
    public ObjectMapper getMapper() {
        return REDIS_OBJECT_MAPPER;
    }

    @Override
    public RedisSerializer<String> keySerializer() {
        return keySerializer;
    }

    @Override
    public ReactiveRedisTemplate<String, String> string() {
        if (Objects.isNull(stringReactiveRedisTemplate)) {
            synchronized (this) {
                if (Objects.isNull(stringReactiveRedisTemplate)) {
                    RedisSerializationContext.RedisSerializationContextBuilder<String, String> builder =
                        RedisSerializationContext.newSerializationContext();
                    StringRedisSerializer valueSerializer = new StringRedisSerializer();
                    builder.key(keySerializer);
                    builder.value(valueSerializer);
                    builder.hashKey(valueSerializer);
                    builder.hashValue(valueSerializer);
                    RedisSerializationContext<String, String> context = builder.build();
                    stringReactiveRedisTemplate = new ReactiveStringRedisTemplate(redisConnectionFactory, context);
                }
            }
        }
        return stringReactiveRedisTemplate;
    }

    @Override
    public <V> ReactiveRedisTemplate<String, V> forType(@Nonnull Type type) {
        JavaType javaType = getMapper().constructType(type);
        return forType(javaType);
    }

    public <T> ReactiveRedisTemplate<String, T> forType(@Nonnull Class<T> type) {
        JavaType javaType = getMapper().constructType(type);
        return forType(javaType);
    }

    @Override
    public <V> ReactiveRedisTemplate<String, V> forType(@Nonnull JavaType javaType) {
        if (!this.redisTemplateMap.containsKey(javaType)) {
            ReactiveRedisTemplate<String, V> template = buildTemplate(javaType);
            this.redisTemplateMap.put(javaType, template);
        }
        return (ReactiveRedisTemplate<String, V>) this.redisTemplateMap.get(javaType);
    }

    private synchronized <T> ReactiveRedisTemplate<String, T> buildTemplate(JavaType type) {
        if (!this.redisTemplateMap.containsKey(type)) {
            Jackson2JsonRedisSerializer<T> valueSerializer = new Jackson2JsonRedisSerializer<>(REDIS_OBJECT_MAPPER, type);

            RedisSerializationContext.RedisSerializationContextBuilder<String, T> builder =
                RedisSerializationContext.newSerializationContext();
            builder.key(keySerializer);
            builder.value(valueSerializer);
            builder.hashKey(new StringRedisSerializer());
            builder.hashValue(valueSerializer);
            RedisSerializationContext<String, T> context = builder.build();
            return new ReactiveRedisTemplate<>(redisConnectionFactory, context);
        } else {
            return (ReactiveRedisTemplate<String, T>) this.redisTemplateMap.get(type);
        }
    }

}
