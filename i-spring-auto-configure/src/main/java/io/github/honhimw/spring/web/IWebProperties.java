package io.github.honhimw.spring.web;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import reactor.core.scheduler.Schedulers;

import java.io.Serializable;

/**
 * @author hon_him
 * @since 2023-06-28
 */

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = IWebProperties.PREFIX)
public class IWebProperties implements Serializable {

    public static final String PREFIX = "i.spring.web";

    private Boolean fallbackHandlerPrintStacktrace = true;

    private Boolean healthyCheckPoint = true;

    private Trace trace = new Trace();

    private Reactive reactive = new Reactive();

    @Getter
    @Setter
    public static class Trace implements Serializable {
        private Boolean enabled = true;

        private Integer length = 8;

        private String reactorContextKey = "MDC_TLA";

        /**
         * Request/Response header
         */
        private String traceHeader = "request-id";

        /**
         * MDC key
         */
        private String traceKey = "traceId";
    }

    @Getter
    @Setter
    public static class Reactive implements Serializable {

        /**
         * Force all http handler to use {@link Schedulers#boundedElastic()}
         */
        private Boolean forceScheduler = true;

    }

}
