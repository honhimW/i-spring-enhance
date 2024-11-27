package io.github.honhimw.spring.cache.redis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.spring.cache.ICacheProperties;
import io.github.honhimw.spring.cache.redis.reactive.R2edisJacksonTemplateFactory;
import io.github.honhimw.spring.cache.redis.reactive.R2edisJacksonTemplateFactoryImpl;
import io.github.honhimw.spring.cache.redis.reactive.R2edisUtils;
import io.github.honhimw.util.JsonUtils;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import reactor.core.publisher.Mono;

import java.util.TimeZone;

/**
 * @author hon_him
 * @since 2022-06-16
 */

@ConditionalOnProperty(value = "i.spring.cache.redis.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(RedisConnectionFactory.class)
@ConditionalOnClass(RedisTemplate.class)
@Configuration
public class IRedisConfiguration {

    private final Environment environment;

    private final ICacheProperties iCacheProperties;

    public IRedisConfiguration(Environment environment, ICacheProperties iCacheProperties) {
        this.environment = environment;
        this.iCacheProperties = iCacheProperties;
    }

    @Bean(name = "redisObjectMapper")
    @ConditionalOnMissingBean(name = "redisObjectMapper")
    ObjectMapper redisObjectMapper() {
        return JsonUtils.mapper().copy()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setTimeZone(TimeZone.getDefault());
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
    Jackson2JsonRedisSerializer<Object> redisValueSerializer() {
        return new Jackson2JsonRedisSerializer<>(redisObjectMapper(), Object.class);
    }

    @Bean(name = "redisJacksonTemplateFactory")
    @ConditionalOnMissingBean(RedisJacksonTemplateFactory.class)
    RedisJacksonTemplateFactory redisJacksonTemplateFactory(RedisConnectionFactory redisConnectionFactory) {
        return new RedisJacksonTemplateFactoryImpl(redisConnectionFactory, redisKeySerializer(), redisObjectMapper());
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

    @Bean(name = "redisEventListener")
    @ConditionalOnMissingBean(RedisEventListenerWrapper.class)
    @ConditionalOnProperty(value = ICacheProperties.Redis.I_SPRING_CACHE_REDIS_ENABLED_EVENT, havingValue = "true")
    RedisEventListenerWrapper redisEventListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        RedisEventListenerWrapper redisEventListenerWrapper = new RedisEventListenerWrapper(redisMessageListenerContainer);
        redisEventListenerWrapper.setKeyspaceNotificationsConfigParameter(iCacheProperties.getRedis().getKeyspaceNotificationsConfigParameter());
        String notificationsTopicPattern = iCacheProperties.getRedis().getNotificationsTopicPattern();
        if (StringUtils.isNotBlank(notificationsTopicPattern)) {
            Topic topic = new PatternTopic(notificationsTopicPattern);
            redisEventListenerWrapper.setTopic(topic);
        }
        return redisEventListenerWrapper;
    }

    @ConditionalOnClass(Mono.class)
    @ConditionalOnBean(ReactiveRedisConnectionFactory.class)
    public class ReactiveRedisConfig {

        @Bean(name = "r2edisJacksonTemplateFactory")
        @ConditionalOnMissingBean(R2edisJacksonTemplateFactory.class)
        R2edisJacksonTemplateFactory r2edisJacksonTemplateFactory(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
            return new R2edisJacksonTemplateFactoryImpl(reactiveRedisConnectionFactory, redisKeySerializer(), redisObjectMapper());
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
