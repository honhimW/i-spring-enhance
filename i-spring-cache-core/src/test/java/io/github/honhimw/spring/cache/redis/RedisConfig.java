package io.github.honhimw.spring.cache.redis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.spring.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.TimeZone;

/**
 * Webflux项目中许多时候需要使用reactive, redis对应的响应式模板{@link ReactiveRedisTemplate}
 *
 * @author hon_him
 * @since 2022-06-16
 */

@EnableCaching
@Configuration
public class RedisConfig {

    @Value("${uac.redis.authorization.prefix:uac:auth}")
    private String prefix;

    private final ObjectMapper REDIS_OBJECT_MAPPER = JsonUtils.getObjectMapper().copy()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setTimeZone(TimeZone.getDefault());

    @Bean
    StringRedisSerializer keySerializer() {
        final String _prefix = StringUtils.appendIfMissing(prefix, ":");
        return new StringRedisSerializer() {
            @Override
            public String deserialize(byte[] bytes) {
                String saveKey = super.deserialize(bytes);
                int indexOf = StringUtils.indexOf(saveKey, _prefix);
                if (indexOf > 0) {
                    saveKey = saveKey.substring(indexOf);
                }
                return saveKey;
            }

            @Override
            public byte[] serialize(String string) {
                return super.serialize(_prefix + string);
            }
        };
    }

    @Bean
    Jackson2JsonRedisSerializer<Object> valueSerializer() {
        return new Jackson2JsonRedisSerializer<>(REDIS_OBJECT_MAPPER, Object.class);
    }

    @Bean
    RedisJacksonTemplateFactory redisJacksonTemplateFactory(RedisConnectionFactory redisConnectionFactory) {
        return new RedisJacksonTemplateFactoryImpl(redisConnectionFactory, new StringRedisSerializer(), JsonUtils.getObjectMapper());
    }

    @Bean
    RedisUtils redisUtils() {
        return new RedisUtils();
    }

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        return redisMessageListenerContainer;
    }

    @Bean
    RedisEventListenerWrapper redisEventListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        return new RedisEventListenerWrapper(redisMessageListenerContainer);
    }

    @Bean
    ReactiveRedisTemplate<String, Object> rxRedisTemplate(
        ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder =
            RedisSerializationContext.newSerializationContext();
        builder.key(keySerializer());
        builder.value(valueSerializer());
        builder.hashKey(new StringRedisSerializer());
        builder.hashValue(valueSerializer());
        RedisSerializationContext<String, Object> build = builder.build();
        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, build);
    }

    @Bean
    public RedisTemplate<String, Object> stringObjectRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(keySerializer());
        redisTemplate.setValueSerializer(valueSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(valueSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }


}
