package io.github.honhimw.spring.web.reactive;

import io.github.honhimw.spring.IDataBufferUtils;
import io.github.honhimw.spring.web.common.HttpLog;
import io.github.honhimw.spring.web.util.MimeTypeSupports;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.*;
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

    public static final int DEFAULT_HANDLER_ORDERED = -1000;

    private final HttpHandler httpHandler;

    private final int ordered;

    public ReactiveHttpLogHandler(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
        this.ordered = DEFAULT_HANDLER_ORDERED;
    }

    public ReactiveHttpLogHandler(HttpHandler httpHandler, int ordered) {
        this.httpHandler = httpHandler;
        this.ordered = ordered;
    }

    @Nonnull
    @Override
    public Mono<Void> handle(@Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response) {
        // only work on TRACE/DEBUG/INFO
        if (!log.isInfoEnabled()) {
            return httpHandler.handle(request, response);
        }

        String methodValue = request.getMethod().name();
        URI uri = request.getURI();

        final HttpLog httpLog = new HttpLog();
        final HttpLog.LogHolder httpLogRef = new HttpLog.LogHolder(httpLog);
        httpLog.setMethod(methodValue);
        httpLog.setUri(uri);

        ServerHttpRequest _request = enhanceRequest(request, httpLog);
        ServerHttpResponse _response = enhanceResponse(response, httpLog);
        long pre = System.currentTimeMillis();
        return httpHandler.handle(_request, _response)
            .contextWrite(context -> context.put(HttpLog.LogHolder.class, httpLogRef))
            .doFinally(signalType -> {
                long post = System.currentTimeMillis();
                long elapsed = post - pre;
                HttpLog finalHttpLog = httpLogRef.get();
                finalHttpLog.setElapsed(elapsed);
                Optional.ofNullable(_response.getStatusCode())
                    .map(HttpStatusCode::value)
                    .ifPresent(finalHttpLog::setStatus);
                {
                    List<Map.Entry<String, String>> requestHeader = new ArrayList<>();
                    request.getHeaders().forEach((key, values) ->
                        values.forEach(value -> requestHeader.add(Map.entry(key, value))));
                    httpLog.setRequestHeaders(requestHeader);
                }
                {
                    List<Map.Entry<String, String>> responseHeader = new ArrayList<>();
                    response.getHeaders().forEach((key, values) ->
                        values.forEach(value -> responseHeader.add(Map.entry(key, value))));
                    httpLog.setResponseHeaders(responseHeader);
                }
                finalHttpLog.log();
            });
    }

    private ServerHttpRequest enhanceRequest(ServerHttpRequest request, HttpLog httpLog) {
        MediaType requestType = request.getHeaders().getContentType();
        if (MimeTypeSupports.isRawType(requestType)) {
            ByteArrayOutputStream baops = httpLog.getRawRequestBody();
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
                if (MimeTypeSupports.isRawType(responseType)) {
                    ByteArrayOutputStream baops = httpLog.getRawResponseBody();
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

    /**
     * set a custom handler ordered by constructor or override this method
     *
     * @return ordered, {@link HttpHandlerDecoratorFactory} ordered also necessary.
     */
    @Override
    public int getOrder() {
        return this.ordered;
    }
}
