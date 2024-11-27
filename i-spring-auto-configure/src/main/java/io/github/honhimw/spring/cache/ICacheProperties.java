package io.github.honhimw.spring.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationEvent;

import java.io.Serializable;
import java.util.Set;

/**
 * @author hon_him
 * @since 2023-06-28
 */

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = ICacheProperties.PREFIX)
public class ICacheProperties implements Serializable {

    public static final String PREFIX = "i.spring.cache";

    private Boolean enabled = true;

    /**
     * Refresh the in-memory cache when events are published.
     */
    private Set<Class<? extends ApplicationEvent>> refreshOnEvent;

    private Redis redis;

    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Redis implements Serializable {

        public static final String REDIS_PREFIX = "redis";

        public static final String I_SPRING_CACHE_REDIS_ENABLED = PREFIX + "." + REDIS_PREFIX + "." + "enabled";

        public static final String I_SPRING_CACHE_REDIS_ENABLED_EVENT = PREFIX + "." + REDIS_PREFIX + "." + "enabled-event";

        public static final String I_SPRING_CACHE_REDIS_KEYSPACE_NOTIFICATIONS_CONFIG_PARAMETER = PREFIX + "." + REDIS_PREFIX + "." + "keyspace-notifications-config-parameter";

        private Boolean enabled = true;

        /**
         * redis event support
         */
        private Boolean enabledEvent = false;

        private String keyspaceNotificationsConfigParameter = "AKE";

        private String notificationsTopicPattern;

    }


}
