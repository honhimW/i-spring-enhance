package io.github.honhimw.spring.web.reactive;

import org.jspecify.annotations.NonNull;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;

/**
 * @author honhimW
 * @since 2025-11-27
 */

@FunctionalInterface
public interface ReactiveHttpLogCondition {

    /**
     * check if the http request/response should be logged before `HttpHandler#handle(ServerHttpRequest, ServerHttpResponse)`
     *
     * @param request reactive http request
     * @return should be logged
     */
    boolean support(ServerHttpRequest request);

    class Delegate implements ReactiveHttpLogCondition {
        private final List<ReactiveHttpLogCondition> delegates;


        public Delegate(List<ReactiveHttpLogCondition> delegates) {
            this.delegates = delegates;
        }

        @Override
        public boolean support(@NonNull ServerHttpRequest request) {
            for (ReactiveHttpLogCondition delegate : delegates) {
                if (!delegate.support(request)) {
                    return false;
                }
            }
            return true;
        }

    }

}
