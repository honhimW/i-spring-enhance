package io.github.honhimw.spring.cache.redis;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
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
public class RedisJacksonTemplateFactoryImpl implements RedisJacksonTemplateFactory {

    private final RedisConnectionFactory redisConnectionFactory;

    private final RedisSerializer<String> keySerializer;

    private final ObjectMapper REDIS_OBJECT_MAPPER;

    private final Map<JavaType, RedisTemplate<String, ?>> redisTemplateMap = new ConcurrentHashMap<>();

    private volatile RedisTemplate<String, String> stringRedisTemplate;

    private volatile RedisTemplate<String, byte[]> bytesRedisTemplate;

    public RedisJacksonTemplateFactoryImpl(RedisConnectionFactory redisConnectionFactory, RedisSerializer<String> keySerializer, ObjectMapper objectMapper) {
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
    public RedisTemplate<String, String> string() {
        if (Objects.isNull(stringRedisTemplate)) {
            synchronized (this) {
                if (Objects.isNull(stringRedisTemplate)) {
                    stringRedisTemplate = new StringRedisTemplate();
                    stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
                    stringRedisTemplate.setKeySerializer(keySerializer);
                    stringRedisTemplate.setValueSerializer(RedisSerializer.string());
                    stringRedisTemplate.setHashKeySerializer(RedisSerializer.string());
                    stringRedisTemplate.setHashValueSerializer(RedisSerializer.string());
                    stringRedisTemplate.afterPropertiesSet();
                }
            }
        }
        return stringRedisTemplate;
    }

    @Override
    public RedisTemplate<String, byte[]> bytes() {
        if (Objects.isNull(bytesRedisTemplate)) {
            synchronized (this) {
                if (Objects.isNull(bytesRedisTemplate)) {
                    bytesRedisTemplate = new RedisTemplate<>();
                    bytesRedisTemplate.setConnectionFactory(redisConnectionFactory);
                    bytesRedisTemplate.setKeySerializer(keySerializer);
                    bytesRedisTemplate.setValueSerializer(RedisSerializer.byteArray());
                    bytesRedisTemplate.setHashKeySerializer(RedisSerializer.string());
                    bytesRedisTemplate.setHashValueSerializer(RedisSerializer.byteArray());
                    bytesRedisTemplate.afterPropertiesSet();
                }
            }
        }
        return bytesRedisTemplate;
    }

    @Override
    public <V> RedisTemplate<String, V> forType(@Nonnull Type type) {
        JavaType javaType = getMapper().constructType(type);
        return forType(javaType);
    }

    @Override
    public <V> RedisTemplate<String, V> forType(@Nonnull Class<V> type) {
        JavaType javaType = getMapper().constructType(type);
        return forType(javaType);
    }

    @Override
    public <V> RedisTemplate<String, V> forType(@Nonnull JavaType javaType) {
        if (!this.redisTemplateMap.containsKey(javaType)) {
            RedisTemplate<String, V> template = buildTemplate(javaType);
            this.redisTemplateMap.put(javaType, template);
        }
        return (RedisTemplate<String, V>) this.redisTemplateMap.get(javaType);
    }

    private synchronized <T> RedisTemplate<String, T> buildTemplate(JavaType type) {
        if (!this.redisTemplateMap.containsKey(type)) {
            Jackson2JsonRedisSerializer<T> valueSerializer = new Jackson2JsonRedisSerializer<>(REDIS_OBJECT_MAPPER, type);
            RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            redisTemplate.setKeySerializer(keySerializer);
            redisTemplate.setValueSerializer(valueSerializer);
            redisTemplate.setHashKeySerializer(new StringRedisSerializer());
            redisTemplate.setHashValueSerializer(valueSerializer);
            redisTemplate.afterPropertiesSet();
            return redisTemplate;
        } else {
            return (RedisTemplate<String, T>) this.redisTemplateMap.get(type);
        }
    }

}
