package io.github.honhimw.spring.cache.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.spring.cache.ICacheProperties;
import io.github.honhimw.spring.cache.redis.reactive.R2edisJacksonTemplateFactory;
import io.github.honhimw.spring.cache.redis.reactive.R2edisJacksonTemplateFactoryImpl;
import io.github.honhimw.spring.cache.redis.reactive.R2edisUtils;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import reactor.core.publisher.Mono;

/**
 * @author hon_him
 * @since 2022-06-16
 */

@ConditionalOnProperty(value = "i.spring.cache.redis.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(RedisConnectionFactory.class)
@ConditionalOnClass(RedisTemplate.class)
@Import({ILettuceConfiguration.class, IJedisConfiguration.class})
@Configuration
public class IRedisConfiguration {

    private final Environment environment;

    public IRedisConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean(name = "redisKeySerializer")
    @ConditionalOnMissingBean(name = "redisKeySerializer")
    StringRedisSerializer redisKeySerializer() {
        String prefix = environment.getProperty("spring.cache.redis.key-prefix", "");
        if (StringUtils.isBlank(prefix)) {
            return new StringRedisSerializer();
        }
        final String _prefix = StringUtils.appendIfMissing(prefix, ":");
        return new StringRedisSerializer() {
            @Override
            public String deserialize(@Nullable byte[] bytes) {
                String saveKey = super.deserialize(bytes);
                return StringUtils.removeStart(saveKey, _prefix);
            }

            @Override
            public byte[] serialize(@Nullable String string) {
                return super.serialize(_prefix + string);
            }
        };
    }

    @Bean(name = "redisValueSerializer")
    @ConditionalOnMissingBean(name = "redisValueSerializer")
    Jackson2JsonRedisSerializer<Object> redisValueSerializer(ObjectMapper objectMapper) {
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }

    @Bean(name = "redisJacksonTemplateFactory")
    @ConditionalOnMissingBean(RedisJacksonTemplateFactory.class)
    RedisJacksonTemplateFactory redisJacksonTemplateFactory(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        return new RedisJacksonTemplateFactoryImpl(redisConnectionFactory, redisKeySerializer(), objectMapper);
    }

    @Bean(name = "jacksonValueRedisTemplate")
    @ConditionalOnMissingBean(name = "jacksonValueRedisTemplate")
    public RedisTemplate<String, Object> jacksonValueRedisTemplate(RedisJacksonTemplateFactory redisJacksonTemplateFactory) {
        return redisJacksonTemplateFactory.forType(Object.class);
    }

    @Bean(name = "redisUtils")
    @ConditionalOnMissingBean(RedisUtils.class)
    RedisUtils redisUtils() {
        return new RedisUtils();
    }

    @Bean(name = "redisMessageListenerContainer")
    @ConditionalOnMissingBean(RedisMessageListenerContainer.class)
    @ConditionalOnProperty(value = ICacheProperties.Redis.I_SPRING_CACHE_REDIS_ENABLED_EVENT, havingValue = "true")
    RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        return redisMessageListenerContainer;
    }

    @ConditionalOnClass(Mono.class)
    @ConditionalOnBean(ReactiveRedisConnectionFactory.class)
    public class ReactiveRedisConfig {

        @Bean(name = "r2edisJacksonTemplateFactory")
        @ConditionalOnMissingBean(R2edisJacksonTemplateFactory.class)
        R2edisJacksonTemplateFactory r2edisJacksonTemplateFactory(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory, ObjectMapper objectMapper) {
            return new R2edisJacksonTemplateFactoryImpl(reactiveRedisConnectionFactory, redisKeySerializer(), objectMapper);
        }

        @Bean(name = "r2JacksonValueRedisTemplate")
        @ConditionalOnMissingBean(name = "r2JacksonValueRedisTemplate")
        ReactiveRedisTemplate<String, Object> r2JacksonValueRedisTemplate(R2edisJacksonTemplateFactory r2edisJacksonTemplateFactory) {
            return r2edisJacksonTemplateFactory.forType(Object.class);
        }

        @Bean(name = "r2edisUtils")
        @ConditionalOnMissingBean(R2edisUtils.class)
        R2edisUtils r2edisUtils() {
            return new R2edisUtils();
        }

    }

}
