package io.github.honhimw.spring.cache.redis;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.KeyspaceEventMessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Currently, Lettuce does not support keyspace events in cluster mode.
 * To implement keyspace events in cluster mode, we need to follow the following steps:
 * 1. Get the node information from the cluster
 * 2. Get the master node
 * 3. Connect to each master node individually and subscribe.
 *
 * @author hon_him
 * @link <a href="https://github.com/spring-projects/spring-data-redis/issues/1111">Keyspace Events in Redis Cluster get lost</a>
 * @see #init()
 * @since 2023-06-26
 */

public class RedisEventListenerWrapper extends KeyspaceEventMessageListener implements ApplicationEventPublisherAware {

    protected final Logger log = LoggerFactory.getLogger("REDIS_EVENT");

    protected final RedisMessageListenerContainer listenerContainer;

    /**
     * Creates new {@link KeyspaceEventMessageListener}.
     *
     * @param listenerContainer must not be {@literal null}.
     */
    public RedisEventListenerWrapper(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
        this.listenerContainer = listenerContainer;
    }

    private ApplicationEventPublisher publisher;

    @Setter
    private Topic topic;

    private String keyspaceNotificationsConfigParameter = "EA";

    private Collection<RedisMessageListenerContainer> nodeContainers;

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

    @Override
    public void setKeyspaceNotificationsConfigParameter(@Nullable String keyspaceNotificationsConfigParameter) {
        this.keyspaceNotificationsConfigParameter = keyspaceNotificationsConfigParameter;
        super.setKeyspaceNotificationsConfigParameter(keyspaceNotificationsConfigParameter);
    }

    @Override
    public void init() {
        RedisConnectionFactory connectionFactory = listenerContainer.getConnectionFactory();
        Assert.notNull(connectionFactory, "listener container must have a connectionFactory reference.");
        try (RedisConnection connection = connectionFactory.getConnection()) {
            if (StringUtils.isNotBlank(keyspaceNotificationsConfigParameter)) {
                Properties config = connection.serverCommands().getConfig("notify-keyspace-events");
                if (Objects.isNull(config) || StringUtils.isBlank(config.getProperty("notify-keyspace-events"))) {
                    connection.serverCommands().setConfig("notify-keyspace-events", keyspaceNotificationsConfigParameter);
                }
            }
            if (connectionFactory instanceof LettuceConnectionFactory lettuceConnectionFactory && connection instanceof RedisClusterConnection clusterConnection) {
                if (Objects.isNull(topic)) {
                    topic = new PatternTopic("__keyevent@*");
                }
                RedisClusterConfiguration clusterConfiguration = lettuceConnectionFactory.getClusterConfiguration();
                Iterable<RedisClusterNode> redisClusterNodes = clusterConnection.clusterGetNodes();
                nodeContainers = new ArrayList<>();
                redisClusterNodes.forEach(clusterNode -> {
                    if (clusterNode.isMaster()) {
                        log.info("master node: {}", clusterNode);
                        String host = clusterNode.getHost();
                        Integer port = clusterNode.getPort();
                        Assert.notNull(host, "host should not be null");
                        Assert.notNull(port, "port should not be null");
                        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration(host, port);
                        Optional.ofNullable(clusterConfiguration)
                            .map(RedisClusterConfiguration::getPassword)
                            .ifPresent(standaloneConfiguration::setPassword);
                        LettuceConnectionFactory lettuceStandaloneConnectionFactory = new LettuceConnectionFactory(standaloneConfiguration);
                        lettuceStandaloneConnectionFactory.afterPropertiesSet();
                        RedisMessageListenerContainer nodeContainer = new RedisMessageListenerContainer();
                        nodeContainer.setConnectionFactory(lettuceStandaloneConnectionFactory);
                        nodeContainer.addMessageListener((message, pattern) -> {
                            if (log.isDebugEnabled()) {
                                String patternString = StringUtils.EMPTY;
                                if (Objects.nonNull(pattern)) {
                                    patternString = new String(pattern, StandardCharsets.UTF_8);
                                }
                                log.debug("receive message from slot: [{}:{}], pattern: [{}]", host, port, patternString);
                            }
                            this.doHandleMessage(message);
                        }, topic);
                        nodeContainer.afterPropertiesSet();
                        nodeContainer.start();
                        nodeContainers.add(nodeContainer);
                    }
                });
            } else {
                if (Objects.isNull(topic) && connectionFactory instanceof LettuceConnectionFactory lettuceConnectionFactory) {
                    RedisStandaloneConfiguration standaloneConfiguration = lettuceConnectionFactory.getStandaloneConfiguration();
                    int database = standaloneConfiguration.getDatabase();
                    setTopic(new PatternTopic("__keyevent@%d__:*".formatted(database)));
                }
                listenerContainer.addMessageListener((message, pattern) -> {
                    if (log.isDebugEnabled()) {
                        String patternString = StringUtils.EMPTY;
                        if (Objects.nonNull(pattern)) {
                            patternString = new String(pattern, StandardCharsets.UTF_8);
                        }
                        log.debug("receive message from standalone, pattern: [{}]", patternString);
                    }
                    this.doHandleMessage(message);
                }, topic);
            }
        } catch (Exception e) {
            log.warn("Redis-Listener initialize error with message: {}", e.toString());
        }
    }

    @Override
    public void destroy() throws Exception {
        if (CollectionUtils.isNotEmpty(nodeContainers)) {
            for (RedisMessageListenerContainer nodeContainer : nodeContainers) {
                nodeContainer.destroy();
            }
        }
        super.destroy();
    }
}
