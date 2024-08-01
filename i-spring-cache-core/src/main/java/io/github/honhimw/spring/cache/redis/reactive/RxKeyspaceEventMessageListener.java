package io.github.honhimw.spring.cache.redis.reactive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.Topic;
import reactor.core.Disposable;

import java.nio.charset.StandardCharsets;

/**
 * @author hon_him
 * @since 2023-06-26
 */

@Slf4j
public abstract class RxKeyspaceEventMessageListener implements InitializingBean, DisposableBean {

    private static final Topic TOPIC_ALL_KEYEVENTS = new PatternTopic("__keyevent@*");

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private Disposable subscribe;

    public RxKeyspaceEventMessageListener(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        subscribe = redisTemplate.listenToLater(TOPIC_ALL_KEYEVENTS)
            .subscribe(flux -> flux.doOnNext(message -> {
                String _channel = message.getChannel();
                String _message = message.getMessage();
                doHandleMessage(new DefaultMessage(_channel.getBytes(StandardCharsets.UTF_8), _message.getBytes(StandardCharsets.UTF_8)));
            }).subscribe());
    }

    @Override
    public void destroy() throws Exception {
        subscribe.dispose();
    }

    protected abstract void doHandleMessage(Message message);
}
