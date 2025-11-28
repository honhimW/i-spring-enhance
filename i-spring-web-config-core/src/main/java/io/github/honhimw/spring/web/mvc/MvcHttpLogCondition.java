package io.github.honhimw.spring.web.mvc;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * @author honhimW
 * @since 2025-11-27
 */

@FunctionalInterface
public interface MvcHttpLogCondition {

    /**
     * check if the http request/response should be logged before `OncePerRequestFilter#doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)`
     *
     * @param request servlet http request
     * @return should be logged
     */
    boolean support(@NonNull HttpServletRequest request);

    class Delegate implements MvcHttpLogCondition {
        private final List<MvcHttpLogCondition> delegates;

        public Delegate(List<MvcHttpLogCondition> delegates) {
            this.delegates = delegates;
        }

        @Override
        public boolean support(@NonNull HttpServletRequest request) {
            for (MvcHttpLogCondition delegate : delegates) {
                if (!delegate.support(request)) {
                    return false;
                }
            }
            return true;
        }
    }

}
