package io.github.honhimw.spring.web.reactive;

import io.github.honhimw.spring.IDataBufferUtils;
import io.github.honhimw.spring.web.common.HttpLog;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.*;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2023-08-02
 */

@Slf4j
public class ReactiveHttpLogHandler implements HttpHandler, Ordered {

    public static final int DEFAULT_HTTP_LOG_HANDLER_ORDERED = -1000;

    private final HttpHandler httpHandler;

    private final int ordered;

    public ReactiveHttpLogHandler(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
        this.ordered = DEFAULT_HTTP_LOG_HANDLER_ORDERED;
    }

    public ReactiveHttpLogHandler(HttpHandler httpHandler, int ordered) {
        this.httpHandler = httpHandler;
        this.ordered = ordered;
    }

    @Nonnull
    @Override
    public Mono<Void> handle(@Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response) {
        // only work on TRACE/DEBUG/INFO level
        if (!log.isInfoEnabled()) {
            return httpHandler.handle(request, response);
        }

        String methodValue = request.getMethod().name();
        URI uri = request.getURI();

        HttpLog httpLog = new HttpLog();
        httpLog.set_method(methodValue);
        httpLog.set_uri(uri);

        ServerHttpRequest _request = enhanceRequest(request, httpLog);
        ServerHttpResponse _response = enhanceResponse(response, httpLog);
        long pre = System.currentTimeMillis();
        return httpHandler.handle(_request, _response)
                .doFinally(signalType -> {
                    long post = System.currentTimeMillis();
                    long cost = post - pre;
                    httpLog.set_serverCost(cost);
                    Optional.ofNullable(_response.getStatusCode())
                            .map(HttpStatusCode::value)
                            .ifPresent(httpLog::set_statusCode);
                    if (log.isTraceEnabled()) {
                        {
                            List<Map.Entry<String, String>> requestHeader = new ArrayList<>();
                            request.getHeaders().forEach((key, values) ->
                                    values.forEach(value -> requestHeader.add(Map.entry(key, value))));
                            httpLog.set_requestHeaders(requestHeader);
                        }
                        {
                            List<Map.Entry<String, String>> responseHeader = new ArrayList<>();
                            response.getHeaders().forEach((key, values) ->
                                    values.forEach(value -> responseHeader.add(Map.entry(key, value))));
                            httpLog.set_responseHeaders(responseHeader);
                        }
                        log.trace(httpLog.fullyInfo());
                    } else if (log.isDebugEnabled()) {
                        log.debug(httpLog.toString());
                    } else if (log.isInfoEnabled()) {
                        log.info(httpLog.baseInfo());
                    }
                });
    }

    private ServerHttpRequest enhanceRequest(ServerHttpRequest request, HttpLog httpLog) {
        MediaType requestType = request.getHeaders().getContentType();
        if (isRawType(requestType)) {
            ByteArrayOutputStream baops = httpLog.get_rawRequestBody();
            return new ServerHttpRequestDecorator(request) {
                @Nonnull
                @Override
                public Flux<DataBuffer> getBody() {
                    ServerHttpRequest delegate = getDelegate();
                    return delegate.getBody()
                            .doOnNext(dataBuffer -> {
                                byte[] bs = IDataBufferUtils.dataBuffer2Bytes(dataBuffer, false);
                                baops.writeBytes(bs);
                            });
                }
            };
        }
        return request;
    }

    private ServerHttpResponse enhanceResponse(ServerHttpResponse response, HttpLog httpLog) {
        return new ServerHttpResponseDecorator(response) {

            @Nonnull
            @Override
            public Mono<Void> writeAndFlushWith(@Nonnull Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return super.writeAndFlushWith(body);
            }

            @Nonnull
            @Override
            public Mono<Void> writeWith(@Nonnull Publisher<? extends DataBuffer> body) {
                MediaType responseType = getDelegate().getHeaders().getContentType();
                if (isRawType(responseType)) {
                    ByteArrayOutputStream baops = httpLog.get_rawResponseBody();
                    if (body instanceof Mono<? extends DataBuffer> bodyMono) {
                        return super.writeWith(bodyMono.doOnNext(dataBuffer -> {
                            byte[] bs = IDataBufferUtils.dataBuffer2Bytes(dataBuffer, false);
                            baops.writeBytes(bs);
                        }));
                    }
                    if (body instanceof Flux<? extends DataBuffer> bodyFlux) {
                        return super.writeWith(bodyFlux.doOnNext(dataBuffer -> {
                            byte[] bs = IDataBufferUtils.dataBuffer2Bytes(dataBuffer, false);
                            baops.writeBytes(bs);
                        }));
                    }
                }
                return super.writeWith(body);
            }
        };
    }

    private boolean isRawType(MimeType mimeType) {
        return MediaType.APPLICATION_JSON.isCompatibleWith(mimeType)
                || MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mimeType)
                || MediaType.APPLICATION_XML.isCompatibleWith(mimeType)
                || MediaType.TEXT_PLAIN.isCompatibleWith(mimeType)
                || MediaType.TEXT_XML.isCompatibleWith(mimeType)
                ;
    }

    /**
     * set a custom handler ordered by constructor or override this method
     * @return ordered, {@link HttpHandlerDecoratorFactory} ordered also necessary.
     */
    @Override
    public int getOrder() {
        return this.ordered;
    }
}
