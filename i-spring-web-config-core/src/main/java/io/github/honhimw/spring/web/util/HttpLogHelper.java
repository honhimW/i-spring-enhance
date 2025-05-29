package io.github.honhimw.spring.web.util;

import io.github.honhimw.spring.web.common.HttpLog;
import org.slf4j.Logger;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @author honhimW
 * @since 2025-04-21
 */

public class HttpLogHelper {

    public static class Webflux {

        public static Mono<Void> off() {
            return log((logger, httpLog) -> {
            });
        }

        public static Mono<Void> info() {
            return log((logger, httpLog) -> {
                if (logger.isInfoEnabled()) {
                    httpLog.info();
                }
            });
        }

        public static Mono<Void> log(BiConsumer<Logger, HttpLog> logger) {
            return Mono.deferContextual(contextView -> Mono.justOrEmpty(contextView.getOrEmpty(HttpLog.LogHolder.class)))
                .cast(HttpLog.LogHolder.class)
                .doOnNext(logHolder -> {
                    HttpLog httpLog = logHolder.get();
                    HttpLog delegate = new HttpLog.Delegate(httpLog) {
                        @Override
                        public void log() {
                            logger.accept(log, httpLog);
                        }
                    };
                    logHolder.set(delegate);
                }).then();
        }

    }

    public static class Mvc {

        public static void off() {
            log((logger, httpLog) -> {
            });
        }

        public static void info() {
            log((logger, httpLog) -> {
                if (logger.isInfoEnabled()) {
                    httpLog.info();
                }
            });
        }

        public static void log(BiConsumer<Logger, HttpLog> logger) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (Objects.nonNull(requestAttributes)) {
                Object attribute = requestAttributes.getAttribute(HttpLog.LogHolder.class.getName(), 0);
                if (attribute instanceof HttpLog.LogHolder logHolder) {
                    HttpLog httpLog = logHolder.get();
                    HttpLog delegate = new HttpLog.Delegate(httpLog) {
                        @Override
                        public void log() {
                            logger.accept(log, httpLog);
                        }
                    };
                    logHolder.set(delegate);
                }
            }

        }

    }

}
