package io.github.honhimw.spring.cache.redis;

import io.github.honhimw.spring.cache.ICacheProperties;
import io.lettuce.core.LettuceVersion;
import io.lettuce.core.protocol.ProtocolVersion;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientOptionsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

/**
 * @author hon_him
 * @since 2024-12-26
 */

@ConditionalOnClass(LettuceVersion.class)
public class ILettuceConfiguration {

    private final ICacheProperties iCacheProperties;

    public ILettuceConfiguration(ICacheProperties iCacheProperties) {
        this.iCacheProperties = iCacheProperties;
    }

    @Bean
    @ConditionalOnProperty(name = "i.spring.cache.redis.protocol")
    LettuceClientOptionsBuilderCustomizer lettuceClientProtocolCustomizer() {
        return builder -> {
            ICacheProperties.Redis.ProtocolVersion protocol = iCacheProperties.getRedis().getProtocol();
            ProtocolVersion protocolVersion = switch (protocol) {
                case RESP2 -> ProtocolVersion.RESP2;
                case RESP3 -> ProtocolVersion.RESP3;
            };
            builder.protocolVersion(protocolVersion);
        };
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
}
