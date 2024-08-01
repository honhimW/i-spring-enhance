package io.github.honhimw.spring.cache.redis.reactive;

import io.github.honhimw.spring.cache.redis.RedisMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import jakarta.annotation.Nonnull;
import java.nio.charset.StandardCharsets;

/**
 * @author hon_him
 * @since 2023-06-26
 */

@Slf4j
public class RedisEventListener extends ReactiveKeyspaceEventMessageListener implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;

    public RedisEventListener(ReactiveRedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    public void setApplicationEventPublisher(@Nonnull ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    @Override
    protected void doHandleMessage(Message message) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        if (log.isDebugEnabled()) {
            log.debug("channel: {}, body: {}", channel, body);
        }
        RedisMessageEvent redisMessageEvent = new RedisMessageEvent(channel, body);
        try {
            publisher.publishEvent(redisMessageEvent);
        } catch (Exception e) {
            log.error("RedisEvent publish error.", e);
        }
    }
}
