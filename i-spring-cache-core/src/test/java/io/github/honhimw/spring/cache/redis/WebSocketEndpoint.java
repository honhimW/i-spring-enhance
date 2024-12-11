package io.github.honhimw.spring.cache.redis;

import io.github.honhimw.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hon_him
 * @since 2023-06-26
 */

@ServerEndpoint("/ws/{key}")
//@Component
public class WebSocketEndpoint {

    private final static Logger log = LoggerFactory.getLogger(WebSocketEndpoint.class);

    private static RedisEventListenerWrapper redisEventListenerWrapper;

    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    @EventListener
    public void redisEvent(RedisMessageEvent event) throws IOException {
        String channel = event.getChannel();
        String body = event.getBody();
        Session session = SESSION_MAP.get(body);
        String[] split = channel.split(":");
        String operation = split[1];
        if (Objects.nonNull(session) && session.isOpen()) {
            String text;
            switch (operation) {
                case "set" -> {
                    text = RedisUtils.get(body);
                    if (StringUtils.isNotBlank(text)) {
                        log.info(text);
                        session.getAsyncRemote().sendText(text);
                    }
                }
                case "del" -> session.getAsyncRemote().sendText(body + " is deleted.");
                case "expire" -> {
                    session.getAsyncRemote().sendText(body + " is expired, session will be close, bye~");
                    session.close();
                    SESSION_MAP.remove(body);
                }
            }
        }
    }

    @Autowired
    public void setRedisEventListener(RedisEventListenerWrapper redisEventListenerWrapper) {
        WebSocketEndpoint.redisEventListenerWrapper = redisEventListenerWrapper;
    }

    private Session session;
    private String uuid;
    private String redisKey;

    @OnOpen
    public void onOpen(Session session, @PathParam("key") String key) throws IOException {
        uuid = UUID.randomUUID().toString();
        log.info("open: {}, {}, {} ", uuid, session.getId(), key);
        this.session = session;
        redisKey = "ws:" + uuid;
        RedisUtils.put(redisKey, key);
        SESSION_MAP.put(redisKey, session);
        session.getAsyncRemote().sendText("hello, we talking by ws now!");
    }

    @OnClose
    public void onClose() throws IOException {
        log.info("close: {}", session.getId());
        Session remove = SESSION_MAP.remove(redisKey);
        if (Objects.nonNull(remove)) {
            remove.close();
        }
    }

    @OnMessage
    public void onMessage(@PathParam("key") String key, String msg) throws IOException {
        log.info("msg: {}", msg);
        Message message = JsonUtils.fromJson(msg, Message.class);
        switch (message.getOpt()) {
            case 1 -> RedisUtils.put(key, message.getValue(), Duration.ofSeconds(message.getTtl()));
            case 2 -> RedisUtils.remove(key);
            case 3 -> this.session.close();
        }
    }

    public static class Message implements Serializable {
        private Integer opt;
        private String value;
        private Long ttl;

        public Integer getOpt() {
            return opt;
        }

        public void setOpt(Integer opt) {
            this.opt = opt;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Long getTtl() {
            return ttl;
        }

        public void setTtl(Long ttl) {
            this.ttl = ttl;
        }
    }


}
