package io.github.honhimw.spring.web.reactive;

import io.github.honhimw.spring.EmptyObjectProvider;
import io.github.honhimw.spring.IDataBufferUtils;
import io.github.honhimw.spring.web.common.HttpLog;
import io.github.honhimw.spring.web.util.MimeTypeSupports;
import org.jspecify.annotations.NonNull;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author hon_him
 * @since 2023-08-02
 */

public class ReactiveHttpLogHandler implements HttpHandler, Ordered {

    public static final int DEFAULT_HANDLER_ORDERED = -1000;

    private final HttpHandler httpHandler;

    private final int ordered;

    private final ReactiveHttpLogCondition confition;

    public ReactiveHttpLogHandler(HttpHandler httpHandler) {
        this(httpHandler, DEFAULT_HANDLER_ORDERED, EmptyObjectProvider.getInstance());
    }

    public ReactiveHttpLogHandler(HttpHandler httpHandler, int ordered) {
        this(httpHandler, ordered, EmptyObjectProvider.getInstance());
    }

    public ReactiveHttpLogHandler(HttpHandler httpHandler, int ordered, ObjectProvider<ReactiveHttpLogCondition> conditions) {
        this.httpHandler = httpHandler;
        this.ordered = ordered;
        this.confition = new ReactiveHttpLogCondition.Delegate(conditions.orderedStream().toList());
    }

    @NonNull
    @Override
    public Mono<Void> handle(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {
        if (!HttpLog.log.isInfoEnabled() || !confition.support(request)) {
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
        if (Objects.nonNull(requestType) && Objects.nonNull(requestType.getCharset())) {
            httpLog.setCharset(requestType.getCharset());
        }
        if (MimeTypeSupports.isRawType(requestType)) {
            ByteArrayOutputStream baops = httpLog.getRawRequestBody();
            return new ServerHttpRequestDecorator(request) {
                @NonNull
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

            @NonNull
            @Override
            public Mono<Void> writeAndFlushWith(@NonNull Publisher<? extends Publisher<? extends DataBuffer>> body) {
                Charset charset = httpLog.getCharset();
                if (HttpLog.log.isDebugEnabled()) {
                    if (body instanceof Flux<? extends Publisher<? extends DataBuffer>> flux) {
                        return super.writeAndFlushWith(
                            flux.flatMap(publisher -> {
                                ByteArrayOutputStream baops = new ByteArrayOutputStream();
                                if (publisher instanceof Flux<? extends DataBuffer> f) {
                                    return Flux.just(f.doOnNext(dataBuffer -> {
                                        byte[] bs = IDataBufferUtils.dataBuffer2Bytes(dataBuffer, false);
                                        baops.writeBytes(bs);
                                    }).doOnComplete(() -> {
                                        String string = baops.toString(charset);
                                        string = HttpLog.snapshot(string);
                                        if (HttpLog.log.isDebugEnabled()) {
                                            HttpLog.log.debug("out >>> {}", string);
                                        }
                                    }));
                                }
                                return flux;
                            })
                        );
                    }
                }
                return super.writeAndFlushWith(body);
            }

            @NonNull
            @Override
            public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
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
