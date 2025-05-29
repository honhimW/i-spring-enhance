package io.github.honhimw.spring.cache.redis;

import io.github.honhimw.spring.cache.ICacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.RedisProtocol;

import static io.github.honhimw.spring.cache.ICacheProperties.Redis.I_SPRING_CACHE_REDIS_PROTOCOL;

/**
 * @author hon_him
 * @since 2024-12-26
 */

@ConditionalOnClass(Jedis.class)
public class IJedisConfiguration {

    private final ICacheProperties iCacheProperties;

    public IJedisConfiguration(ICacheProperties iCacheProperties) {
        this.iCacheProperties = iCacheProperties;
    }

    @Bean
    @ConditionalOnProperty(name = I_SPRING_CACHE_REDIS_PROTOCOL)
    JedisClientConfigurationBuilderCustomizer jedisClientProtocolCustomizer() {
        return builder -> {
            ICacheProperties.Redis.ProtocolVersion protocol = iCacheProperties.getRedis().getProtocol();
            RedisProtocol redisProtocol = switch (protocol) {
                case RESP2 -> RedisProtocol.RESP2;
                case RESP3 -> RedisProtocol.RESP3;
            };
            builder.customize(builder1 -> builder1
                .protocol(redisProtocol)
            );
        };
    }

}
