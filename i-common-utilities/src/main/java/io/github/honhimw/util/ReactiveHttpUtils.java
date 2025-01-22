/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.honhimw.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProtocols;
import io.netty.resolver.AddressResolverGroup;
import jakarta.annotation.Nullable;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClient.RequestSender;
import reactor.netty.http.client.HttpClient.ResponseReceiver;
import reactor.netty.http.client.HttpClientForm;
import reactor.netty.http.client.HttpClientResponse;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.TransportConfig;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <table>
 *     <caption>Properties default values</caption>
 *     <tr style="color:yellow">
 *         <th>Property</th>
 *         <th>default value</th>
 *     </tr>
 *     <tr>
 *         <td>connect timeout</td>
 *         <td>4s</td>
 *     </tr>
 *     <tr>
 *         <td>read timeout</td>
 *         <td>30s</td>
 *     </tr>
 *     <tr>
 *         <td>HTTP Protocol</td>
 *         <td>HTTP/1.1, HTTP/2</td>
 *     </tr>
 *     <tr>
 *         <td>follow redirect</td>
 *         <td>true</td>
 *     </tr>
 *     <tr>
 *         <td>keepalive</td>
 *         <td>true</td>
 *     </tr>
 *     <tr>
 *         <td>proxy with system properties</td>
 *         <td>true</td>
 *     </tr>
 *     <tr>
 *         <td>compress</td>
 *         <td>true</td>
 *     </tr>
 *     <tr>
 *         <td>retry</td>
 *         <td>true</td>
 *     </tr>
 *     <tr>
 *         <td>ssl</td>
 *         <td>false</td>
 *     </tr>
 *     <tr>
 *         <td>max connections</td>
 *         <td>1000</td>
 *     </tr>
 *     <tr>
 *         <td>pending acquire max count</td>
 *         <td>1000</td>
 *     </tr>
 * </table>
 *
 * @author hon_him
 * @see RequestConfig.Builder default request configuration
 * @since 2023-02-22
 */
@SuppressWarnings({
    "unused",
    "UnusedReturnValue",
})
public class ReactiveHttpUtils implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ReactiveHttpUtils.class);

    private ObjectMapper objectMapper;

    public void setObjectMapper(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper);
        this.objectMapper = objectMapper;
    }

    private ReactiveHttpUtils() {
    }

    static class NoopConnectProvider implements ConnectionProvider {

        static final NoopConnectProvider INSTANCE = new NoopConnectProvider();

        @Nonnull
        @Override
        public Mono<? extends Connection> acquire(@Nonnull TransportConfig config, @Nonnull ConnectionObserver connectionObserver, Supplier<? extends SocketAddress> remoteAddress, AddressResolverGroup<?> resolverGroup) {
            return Mono.error(() -> new IllegalStateException("http-client already closed."));
        }

        @Override
        public boolean isDisposed() {
            return true;
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            if (!Objects.equals(connectionProvider, NoopConnectProvider.INSTANCE)) {
                connectionProvider.dispose();
                connectionProvider = NoopConnectProvider.INSTANCE;
                httpClient = HttpClient.create(connectionProvider);
            } else {
                throw new IllegalStateException("already closed.");
            }
        }
    }

    /**
     * GET
     */
    public static final String METHOD_GET = "GET";
    /**
     * POST
     */
    public static final String METHOD_POST = "POST";
    /**
     * PUT
     */
    public static final String METHOD_PUT = "PUT";
    /**
     * PATCH
     */
    public static final String METHOD_PATCH = "PATCH";
    /**
     * DELETE
     */
    public static final String METHOD_DELETE = "DELETE";
    /**
     * OPTIONS
     */
    public static final String METHOD_OPTIONS = "OPTIONS";
    /**
     * HEAD
     */
    public static final String METHOD_HEAD = "HEAD";

    /**
     * Construct a {@link ReactiveHttpUtils} instance
     *
     * @return {@link ReactiveHttpUtils}
     */
    public static ReactiveHttpUtils getInstance() {
        ReactiveHttpUtils reactiveHttpUtils = new ReactiveHttpUtils();
        reactiveHttpUtils.init();
        return reactiveHttpUtils;
    }

    /**
     * Construct a {@link ReactiveHttpUtils} instance with custom {@link RequestConfig.Builder}
     *
     * @param configurer custom {@link RequestConfig.Builder}
     * @return {@link ReactiveHttpUtils}
     */
    public static ReactiveHttpUtils getInstance(Consumer<RequestConfig.Builder> configurer) {
        ReactiveHttpUtils reactiveHttpUtils = new ReactiveHttpUtils();
        RequestConfig.Builder copy = RequestConfig.DEFAULT_CONFIG.copy();
        configurer.accept(copy);
        RequestConfig requestConfig = copy.build();
        reactiveHttpUtils.init(requestConfig);
        return reactiveHttpUtils;
    }

    /**
     * max total connections
     */
    public static final int MAX_TOTAL_CONNECTIONS = 1_000;

    /**
     * max connections per route
     */
    public static final int MAX_ROUTE_CONNECTIONS = 200;

    /**
     * connect timeout
     */
    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(4);

    /**
     * read timeout
     */
    public static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    /**
     * charset
     */
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private HttpClient httpClient;

    private ConnectionProvider connectionProvider;

    private RequestConfig _defaultRequestConfig;

    private ChainBuilder chainBuilder;

    private void init() {
        init(RequestConfig.DEFAULT_CONFIG);
    }

    private void init(RequestConfig requestConfig) {
        connectionProvider = requestConfig.connectionProvider;
        httpClient = HttpClient.create(connectionProvider);
        httpClient = requestConfig.config(httpClient);
        chainBuilder = new ChainBuilder();
        requestConfig.chainBuilder.accept(chainBuilder);
        _defaultRequestConfig = requestConfig;
        objectMapper = requestConfig.objectMapper;
    }

    /**
     * Sends an HTTP GET request to the specified URL.
     *
     * @param url the URL to send the request to
     * @return the result of the HTTP request
     */
    public HttpResult get(String url) {
        return request(METHOD_GET, url);
    }

    /**
     * Sends an HTTP POST request to the specified URL.
     *
     * @param url the URL to send the request to
     * @return the result of the HTTP request
     */
    public HttpResult post(String url) {
        return request(METHOD_POST, url);
    }

    /**
     * Sends an HTTP PUT request to the specified URL.
     *
     * @param url the URL to send the request to
     * @return the result of the HTTP request
     */
    public HttpResult put(String url) {
        return request(METHOD_PUT, url);
    }

    /**
     * Sends an HTTP PATCH request to the specified URL.
     *
     * @param url the URL to send the request to
     * @return the result of the HTTP request
     */
    public HttpResult patch(String url) {
        return request(METHOD_PATCH, url);
    }

    /**
     * Sends an HTTP DELETE request to the specified URL.
     *
     * @param url the URL to send the request to
     * @return the result of the HTTP request
     */
    public HttpResult delete(String url) {
        return request(METHOD_DELETE, url);
    }

    /**
     * Sends an HTTP OPTIONS request to the specified URL.
     *
     * @param url the URL to send the request to
     * @return the result of the HTTP request
     */
    public HttpResult options(String url) {
        return request(METHOD_OPTIONS, url);
    }

    /**
     * Sends an HTTP HEAD request to the specified URL.
     *
     * @param url the URL to send the request to
     * @return the result of the HTTP request
     */
    public HttpResult head(String url) {
        return request(METHOD_HEAD, url);
    }

    /**
     * Sends an HTTP GET request to the specified URL, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the result of the HTTP request
     */
    public HttpResult get(String url, Consumer<Configurer> configurer) {
        return request(METHOD_GET, url, configurer);
    }

    /**
     * Sends an HTTP POST request to the specified URL, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the result of the HTTP request
     */
    public HttpResult post(String url, Consumer<Configurer> configurer) {
        return request(METHOD_POST, url, configurer);
    }

    /**
     * Sends an HTTP PUT request to the specified URL, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the result of the HTTP request
     */
    public HttpResult put(String url, Consumer<Configurer> configurer) {
        return request(METHOD_PUT, url, configurer);
    }

    /**
     * Sends an HTTP PATCH request to the specified URL, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the result of the HTTP request
     */
    public HttpResult patch(String url, Consumer<Configurer> configurer) {
        return request(METHOD_PATCH, url, configurer);
    }

    /**
     * Sends an HTTP DELETE request to the specified URL, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the result of the HTTP request
     */
    public HttpResult delete(String url, Consumer<Configurer> configurer) {
        return request(METHOD_DELETE, url, configurer);
    }

    /**
     * Sends an HTTP OPTIONS request to the specified URL, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the result of the HTTP request
     */
    public HttpResult options(String url, Consumer<Configurer> configurer) {
        return request(METHOD_OPTIONS, url, configurer);
    }

    /**
     * Sends an HTTP HEAD request to the specified URL, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the result of the HTTP request
     */
    public HttpResult head(String url, Consumer<Configurer> configurer) {
        return request(METHOD_HEAD, url, configurer);
    }

    /**
     * Sends an HTTP request to the specified URL, without configurer.
     *
     * @return the result of the HTTP request
     */
    public HttpResult request(Consumer<Configurer> configurer) {
        return request(null, null, configurer);
    }

    /**
     * Sends an HTTP request to the specified URL, without configurer.
     *
     * @param method the method of the request
     * @param url    the URL to send the request to
     * @return the result of the HTTP request
     */
    public HttpResult request(String method, String url) {
        return request(method, url, configurer -> {
        });
    }

    /**
     * Sends an HTTP request to the specified URL, with the specified configurer.
     *
     * @param method     the method of the request
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the result of the HTTP request
     */
    public HttpResult request(String method, String url, Consumer<Configurer> configurer) {
        return request(method, url, configurer, httpResult -> httpResult);
    }

    /**
     * Sends an HTTP GET request to the specified URL in reactive.
     *
     * @param url the URL to send the request to
     * @return the reactive result of the HTTP request
     */
    public ResponseReceiver<?> rGet(String url) {
        return receiver(METHOD_GET, url, configurer -> {
        });
    }

    /**
     * Sends an HTTP POST request to the specified URL in reactive.
     *
     * @param url the URL to send the request to
     * @return the reactive result of the HTTP request
     */
    public ResponseReceiver<?> rPost(String url) {
        return receiver(METHOD_POST, url, configurer -> {
        });
    }

    /**
     * Sends an HTTP PUT request to the specified URL in reactive.
     *
     * @param url the URL to send the request to
     * @return the reactive result of the HTTP request
     */
    public ResponseReceiver<?> rPut(String url) {
        return receiver(METHOD_PUT, url, configurer -> {
        });
    }

    /**
     * Sends an HTTP DELETE request to the specified URL in reactive.
     *
     * @param url the URL to send the request to
     * @return the reactive result of the HTTP request
     */
    public ResponseReceiver<?> rDelete(String url) {
        return receiver(METHOD_DELETE, url, configurer -> {
        });
    }

    /**
     * Sends an HTTP OPTIONS request to the specified URL in reactive.
     *
     * @param url the URL to send the request to
     * @return the reactive result of the HTTP request
     */
    public ResponseReceiver<?> rOptions(String url) {
        return receiver(METHOD_OPTIONS, url, configurer -> {
        });
    }

    /**
     * Sends an HTTP HEAD request to the specified URL in reactive.
     *
     * @param url the URL to send the request to
     * @return the reactive result of the HTTP request
     */
    public ResponseReceiver<?> rHead(String url) {
        return receiver(METHOD_HEAD, url, configurer -> {
        });
    }

    /**
     * Sends an HTTP GET request to the specified URL in reactive, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the reactive result of the HTTP request
     */
    public ResponseReceiver<?> rGet(String url, Consumer<Configurer> configurer) {
        return receiver(METHOD_GET, url, configurer);
    }

    /**
     * Sends an HTTP POST request to the specified URL in reactive, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the reactive result of the HTTP request
     */
    public ResponseReceiver<?> rPost(String url, Consumer<Configurer> configurer) {
        return receiver(METHOD_POST, url, configurer);
    }

    /**
     * Sends an HTTP PUT request to the specified URL in reactive, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the reactive result of the HTTP request
     */
    public ResponseReceiver<?> rPut(String url, Consumer<Configurer> configurer) {
        return receiver(METHOD_PUT, url, configurer);
    }

    /**
     * Sends an HTTP PATCH request to the specified URL in reactive, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the reactive result of the HTTP request
     */
    public ResponseReceiver<?> rPatch(String url, Consumer<Configurer> configurer) {
        return receiver(METHOD_PATCH, url, configurer);
    }

    /**
     * Sends an HTTP DELETE request to the specified URL in reactive, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the reactive result of the HTTP request
     */
    public ResponseReceiver<?> rDelete(String url, Consumer<Configurer> configurer) {
        return receiver(METHOD_DELETE, url, configurer);
    }

    /**
     * Sends an HTTP OPTIONS request to the specified URL in reactive, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the reactive result of the HTTP request
     */
    public ResponseReceiver<?> rOptions(String url, Consumer<Configurer> configurer) {
        return receiver(METHOD_OPTIONS, url, configurer);
    }

    /**
     * Sends an HTTP HEAD request to the specified URL in reactive, with the specified configurer.
     *
     * @param url        the URL to send the request to
     * @param configurer configurer of the request
     * @return the reactive result of the HTTP request
     */
    public ResponseReceiver<?> rHead(String url, Consumer<Configurer> configurer) {
        return receiver(METHOD_HEAD, url, configurer);
    }

    /**
     * blocking request
     *
     * @param method       HTTP method
     * @param url          HTTP url
     * @param configurer   configurer of the request
     * @param resultMapper result mapping
     * @param <T>          result type
     * @return the result
     */
    public <T> T request(String method, String url, Consumer<Configurer> configurer,
                         Function<HttpResult, T> resultMapper) {
        ResponseReceiver<?> receiver = receiver(method, url, configurer);
        Consumer<Configurer> _configurer = conf -> conf
            .method(method)
            .charset(DEFAULT_CHARSET)
            .url(url);
        _configurer = _configurer.andThen(configurer);
        return resultMapper.apply(execute(_configurer).block());
    }

    /**
     * Reactive request
     *
     * @param configurer configurer of the request
     * @return the reactive result
     */
    public ResponseReceiver<?> receiver(Consumer<Configurer> configurer) {
        return receiver(null, null, configurer);
    }

    /**
     * Reactive request
     *
     * @param method     http method
     * @param url        http url
     * @param configurer configurer of the request
     * @return the reactive result
     */
    public ResponseReceiver<?> receiver(String method, String url, Consumer<Configurer> configurer) {
        _assertState(Objects.nonNull(configurer), "String should not be null");
        Consumer<Configurer> _configurer = conf -> conf
            .method(method)
            .charset(DEFAULT_CHARSET)
            .url(url);
        _configurer = _configurer.andThen(configurer);
        Configurer requestConfigurer = new Configurer(this, _defaultRequestConfig);
        _configurer.accept(requestConfigurer);
        _assertState(StringUtils.isNotBlank(requestConfigurer.method()), "Method should not be blank");
        _assertState(StringUtils.isNotBlank(requestConfigurer.url()), "URL should not be blank");
        return toReceiver(requestConfigurer);
    }

    private Mono<HttpResult> execute(Consumer<Configurer> configurer) {
        Configurer requestConfigurer = new Configurer(this, _defaultRequestConfig);
        configurer.accept(requestConfigurer);
        _assertState(StringUtils.isNotBlank(requestConfigurer.method()), "Method should not be blank");
        _assertState(StringUtils.isNotBlank(requestConfigurer.url()), "URL should not be blank");
        try {
            FilterContext filterContext = new FilterContext(this, httpClient, requestConfigurer);
            FilterChain filterChain = chainBuilder.build();
            return filterChain.doFilter(filterContext);
        } catch (Exception e) {
            throw new IllegalStateException("HTTP Execution Error", e);
        }
    }

    private ResponseReceiver<?> toReceiver(Configurer configurer) {
        HttpClient client = Optional.ofNullable(configurer.config)
            .map(requestConfig -> requestConfig.config(httpClient))
            .orElse(httpClient);

        Configurer.AbstractBody<?> body = Optional.ofNullable(configurer.bodyConfigurer)
            .map(bodyModelConsumer -> {
                Configurer.Payload payload = new Configurer.Payload(configurer);
                bodyModelConsumer.accept(payload);
                return payload.getBody();
            }).orElse(null);
        RequestSender requestSender = client.request(HttpMethod.valueOf(configurer.method));

        URI uri;
        try {
            uri = new URIBuilder(configurer.url, configurer.charset).addParameters(configurer.params).build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        requestSender = requestSender.uri(uri);

        if (Objects.nonNull(body)) {
            body.init();
            return body.sender(requestSender, configurer);
        }
        return requestSender;
    }

    /**
     * Request config
     */
    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RequestConfig {

        /**
         * Default config
         */
        public static RequestConfig DEFAULT_CONFIG = RequestConfig.builder().build();

        private final Duration connectTimeout;
        private final Duration readTimeout;
        private final HttpProtocol[] httpProtocols;
        private final String[] sslProtocols;
        private final boolean followRedirect;
        private final boolean keepalive;
        private final boolean proxyWithSystemProperties;
        private final boolean enableCompress;
        private final boolean enableRetry;
        private final boolean noSSL;
        private final ConnectionProvider connectionProvider;
        private final Function<HttpClient, HttpClient> customize;
        private final Consumer<ChainBuilder> chainBuilder;
        private final ObjectMapper objectMapper;

        private HttpClient config(HttpClient httpClient) {
            HttpClient client = httpClient;
            client = client
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Long.valueOf(connectTimeout.toMillis()).intValue())
                .option(ChannelOption.SO_KEEPALIVE, keepalive)
                .protocol(httpProtocols)
                .keepAlive(keepalive)
                .followRedirect(followRedirect)
                .compress(enableCompress)
                .disableRetry(!enableRetry)
                .responseTimeout(readTimeout)
            ;
            if (proxyWithSystemProperties) {
                client = client.proxyWithSystemProperties();
            }
            if (noSSL) {
                client = client.noSSL();
            } else {
                client = client.secure(sslContextSpec -> {
                    try {
                        sslContextSpec.sslContext(SslContextBuilder.forClient().protocols(this.sslProtocols).build());
                    } catch (SSLException e) {
                        throw new IllegalArgumentException("Unable to create SSLContext", e);
                    }
                });
            }
            // customize
            client = customize.apply(client);
            Objects.requireNonNull(client);

            return client;
        }

        private Builder copy() {
            return copy(this);
        }

        private static Builder copy(RequestConfig config) {
            Builder builder = RequestConfig.builder();
            builder.connectTimeout(config.connectTimeout);
            builder.readTimeout(config.readTimeout);
            builder.httpProtocol(config.httpProtocols);
            builder.followRedirect(config.followRedirect);
            builder.keepalive(config.keepalive);
            builder.proxyWithSystemProperties(config.proxyWithSystemProperties);
            builder.enableCompress(config.enableCompress);
            builder.enableRetry(config.enableRetry);
            builder.noSSL(config.noSSL);
            builder.connectionProvider(config.connectionProvider);
            builder.customize(config.customize);
            builder.chainBuilder = config.chainBuilder;
            return builder;
        }

        /**
         * Creates and returns a new instance of the Builder class.
         *
         * @return a new instance of the Builder class
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Request configuration Builder, initial with default setting
         */
        public static class Builder {

            private Duration connectTimeout = CONNECT_TIMEOUT;
            private Duration readTimeout = READ_TIMEOUT;
            private HttpProtocol[] httpProtocols = {HttpProtocol.HTTP11, HttpProtocol.H2C};
            private String[] sslProtocols = {SslProtocols.TLS_v1_3, SslProtocols.TLS_v1_2};
            private boolean followRedirect = true;
            private boolean keepalive = true;
            private boolean proxyWithSystemProperties = true;
            private boolean enableCompress = true;
            private boolean enableRetry = true;
            private boolean noSSL = true;
            private ConnectionProvider connectionProvider = ConnectionProvider.builder("ReactiveHttpUtils")
                .maxConnections(MAX_TOTAL_CONNECTIONS)
                .pendingAcquireMaxCount(MAX_TOTAL_CONNECTIONS)
                .build();
            private Function<HttpClient, HttpClient> customize = _httpClient -> _httpClient;
            private Consumer<ChainBuilder> chainBuilder = ChainBuilder::withDefault;
            private ObjectMapper objectMapper = JsonUtils.mapper().copy();

            /**
             * Configure the connect timeout
             *
             * @param connectTimeout connect timeout
             * @return this
             */
            public Builder connectTimeout(Duration connectTimeout) {
                this.connectTimeout = connectTimeout;
                return this;
            }

            /**
             * Configure the read timeout
             *
             * @param readTimeout read timeout
             * @return this
             */
            public Builder readTimeout(Duration readTimeout) {
                this.readTimeout = readTimeout;
                return this;
            }

            /**
             * Configure the http protocol
             *
             * @param httpProtocols http protocol
             * @return this
             */
            public Builder httpProtocol(HttpProtocol... httpProtocols) {
                this.httpProtocols = httpProtocols;
                return this;
            }

            /**
             * Configure the ssl protocols
             *
             * @param sslProtocols ssl protocols
             * @return this
             * @see SslProtocols
             * @see #noSSL(boolean) if you want to enable ssl
             */
            public Builder sslProtocols(String... sslProtocols) {
                this.sslProtocols = sslProtocols;
                return this;
            }

            /**
             * Configure the follow redirect
             *
             * @param followRedirect follow redirect
             * @return this
             */
            public Builder followRedirect(boolean followRedirect) {
                this.followRedirect = followRedirect;
                return this;
            }

            /**
             * Configure the keepalive
             *
             * @param keepalive keepalive
             * @return this
             */
            public Builder keepalive(boolean keepalive) {
                this.keepalive = keepalive;
                return this;
            }

            /**
             * Configure the proxy with system properties
             *
             * @param proxyWithSystemProperties proxy with system properties
             * @return this
             */
            public Builder proxyWithSystemProperties(boolean proxyWithSystemProperties) {
                this.proxyWithSystemProperties = proxyWithSystemProperties;
                return this;
            }

            /**
             * Configure the enable compress
             *
             * @param enableCompress enable compress
             * @return this
             */
            public Builder enableCompress(boolean enableCompress) {
                this.enableCompress = enableCompress;
                return this;
            }

            /**
             * Configure the enable retry
             *
             * @param enableRetry enable retry
             * @return this
             */
            public Builder enableRetry(boolean enableRetry) {
                this.enableRetry = enableRetry;
                return this;
            }

            /**
             * Configure the no ssl
             *
             * @param noSSL no ssl
             * @return this
             */
            public Builder noSSL(boolean noSSL) {
                this.noSSL = noSSL;
                return this;
            }

            /**
             * Configure the connection provider
             *
             * @param connectionProvider connection provider
             * @return this
             */
            public Builder connectionProvider(ConnectionProvider connectionProvider) {
                this.connectionProvider = connectionProvider;
                return this;
            }

            /**
             * Customize the http client
             *
             * @param customize customize
             * @return this
             */
            public Builder customize(Function<HttpClient, HttpClient> customize) {
                this.customize = this.customize.andThen(customize);
                return this;
            }

            /**
             * Configure the filter
             *
             * @param filters filters
             * @return this
             */
            public Builder filters(Consumer<ChainBuilder> filters) {
                this.chainBuilder = this.chainBuilder.andThen(filters);
                return this;
            }

            /**
             * Configure the objectMapper
             *
             * @param objectMapper objectMapper
             * @return this
             */
            public Builder objectMapper(ObjectMapper objectMapper) {
                this.objectMapper = objectMapper;
                return this;
            }

            /**
             * Build the RequestConfig
             *
             * @return RequestConfig
             */
            public RequestConfig build() {
                return new RequestConfig(
                    connectTimeout,
                    readTimeout,
                    httpProtocols,
                    sslProtocols,
                    followRedirect,
                    keepalive,
                    proxyWithSystemProperties,
                    enableCompress,
                    enableRetry,
                    noSSL,
                    connectionProvider,
                    customize,
                    chainBuilder,
                    objectMapper
                );
            }
        }

    }

    /**
     * Configurer
     */
    public final static class Configurer {

        private final ReactiveHttpUtils self;

        private final RequestConfig currentDefaultConfig;

        private Configurer(ReactiveHttpUtils self, RequestConfig currentDefaultConfig) {
            this.self = self;
            this.currentDefaultConfig = currentDefaultConfig;
        }

        private String method;

        private Charset charset;

        private String url;

        private final Map<CharSequence, List<CharSequence>> headers = new HashMap<>();

        private final List<Entry<String, String>> params = new ArrayList<>();

        private Consumer<Payload> bodyConfigurer;

        private RequestConfig config;

        /**
         * Configure the method
         *
         * @param method http method
         * @return this
         */
        public Configurer method(String method) {
            this.method = method;
            return this;
        }

        /**
         * GET
         *
         * @return this
         */
        public Configurer get() {
            this.method = METHOD_GET;
            return this;
        }

        /**
         * POST
         *
         * @return this
         */
        public Configurer post() {
            this.method = METHOD_POST;
            return this;
        }

        /**
         * PUT
         *
         * @return this
         */
        public Configurer put() {
            this.method = METHOD_PUT;
            return this;
        }

        /**
         * DELETE
         *
         * @return this
         */
        public Configurer delete() {
            this.method = METHOD_DELETE;
            return this;
        }

        /**
         * PATCH
         *
         * @return this
         */
        public Configurer patch() {
            this.method = METHOD_PATCH;
            return this;
        }

        /**
         * Configure the charset
         *
         * @param charset charset
         * @return this
         */
        public Configurer charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        /**
         * Configure the url
         *
         * @param url url
         * @return this
         */
        public Configurer url(String url) {
            this.url = url;
            return this;
        }

        /**
         * Add header pair
         *
         * @param name  header name
         * @param value header value
         * @return this
         */
        public Configurer header(CharSequence name, CharSequence value) {
            List<CharSequence> list = this.headers.get(name);
            if (Objects.isNull(list)) {
                list = new ArrayList<>();
                this.headers.put(name, list);
            }
            list.add(value);
            return this;
        }

        /**
         * Add header pair only if absent
         *
         * @param name  header name
         * @param value header value
         * @return this
         */
        public Configurer headerIfAbsent(CharSequence name, CharSequence value) {
            List<CharSequence> list = this.headers.get(name);
            if (Objects.isNull(list)) {
                list = new ArrayList<>();
                this.headers.put(name, list);
                list.add(value);
            }
            return this;
        }

        /**
         * Override headers
         *
         * @param headers headers
         * @return this
         */
        public Configurer headers(Map<? extends CharSequence, ? extends CharSequence> headers) {
            headers.forEach(this::header);
            return this;
        }

        /**
         * Add query parameter pair
         *
         * @param name  parameter name
         * @param value parameter value
         * @return this
         */
        public Configurer param(String name, String value) {
            params.add(new AbstractMap.SimpleImmutableEntry<>(name, value));
            return this;
        }

        /**
         * Override query parameters
         *
         * @param params parameters
         * @return this
         */
        public Configurer params(Map<String, String> params) {
            params.forEach(this::param);
            return this;
        }

        /**
         * Configure request config
         *
         * @param config request config
         * @return this
         */
        public Configurer config(RequestConfig config) {
            this.config = config;
            return this;
        }

        /**
         * Configure request config
         *
         * @param consumer request config
         * @return this
         */
        public Configurer config(Consumer<RequestConfig.Builder> consumer) {
            RequestConfig.Builder copy;
            if (Objects.isNull(config)) {
                copy = currentDefaultConfig.copy();
            } else {
                copy = config.copy();
            }
            consumer.accept(copy);
            this.config = copy.build();
            return this;
        }

        /**
         * Sets the body configurer for the function.
         *
         * @param configurer the consumer to configure the payload
         * @return the updated Configurer object
         */
        public Configurer body(Consumer<Payload> configurer) {
            bodyConfigurer = configurer;
            return this;
        }

        /**
         * Get current method
         *
         * @return http method
         */
        public String method() {
            return this.method;
        }

        /**
         * Get current charset
         *
         * @return charset
         */
        public Charset charset() {
            return this.charset;
        }

        /**
         * Get current url
         *
         * @return http url
         */
        public String url() {
            return this.url;
        }

        /**
         * Get current parameters
         *
         * @return parameters
         */
        public List<Entry<String, String>> params() {
            return this.params;
        }

        /**
         * Get current headers
         *
         * @return headers
         */
        public Map<CharSequence, List<CharSequence>> headers() {
            return this.headers;
        }

        /**
         * HTTP request payload entity
         */
        public static class Payload {

            private final Configurer self;

            public Payload(Configurer self) {
                this.self = self;
            }

            private AbstractBody<?> body;

            /**
             * Get configured body
             *
             * @return configured body
             */
            protected AbstractBody<?> getBody() {
                return body;
            }

            /**
             * Raw type payload
             *
             * @param configurer raw configurer
             * @return this
             */
            public Payload raw(Consumer<Raw> configurer) {
                return type(() -> new Raw(self.self.objectMapper), configurer);
            }

            /**
             * Form Data payload
             *
             * @param configurer form-data configurer
             * @return this
             */
            public Payload formData(Consumer<FormData> configurer) {
                return type(FormData::new, configurer);
            }

            /**
             * Binary payload
             *
             * @param configurer binary configurer
             * @return this
             */
            public Payload binary(Consumer<Binary> configurer) {
                return type(Binary::new, configurer);
            }

            /**
             * Form Url Encoded payload
             *
             * @param configurer form-url-encoded configurer
             * @return this
             */
            public Payload formUrlEncoded(Consumer<FormUrlEncoded> configurer) {
                return type(FormUrlEncoded::new, configurer);
            }

            /**
             * Other type payload
             *
             * @param buildable  body supplier
             * @param configurer body configurer
             * @param <T>        Body type
             * @return this
             */
            public <T extends AbstractBody<T>> Payload type(
                Supplier<T> buildable, Consumer<T> configurer) {
                Objects.requireNonNull(buildable);
                Objects.requireNonNull(configurer);
                if (Objects.isNull(body)) {
                    T built = buildable.get();
                    if (Objects.nonNull(built)) {
                        configurer.accept(built);
                        body = built;
                    }
                }
                return this;
            }
        }

        /**
         * Payload body abstract class
         */
        public static abstract class AbstractBody<I extends AbstractBody<I>> {

            protected String contentType;

            /**
             * Body initialize
             */
            protected void init() {

            }

            /**
             * Note: May not work in some cases, netty-http will set automatically.
             * Most of the time you won't need to set the content type manually.
             * If you do need to set the content type manually, call this method after the body is built.
             * Because the content type is set automatically while building the body.
             *
             * @param contentType custom content type
             * @return this
             */
            @SuppressWarnings("unchecked")
            public I contentType(@Nullable String contentType) {
                this.contentType = contentType;
                return (I) this;
            }

            /**
             * Body payload content-type define
             *
             * @return content-type
             */
            protected String contentType() {
                return contentType;
            }

            /**
             * Do on sender.
             *
             * @param sender     sender
             * @param configurer configurer
             * @return response receiver
             */
            protected abstract ResponseReceiver<?> sender(RequestSender sender, Configurer configurer);
        }

        /**
         * Raw type support
         */
        public static class Raw extends AbstractBody<Raw> {

            /**
             * text/plain content-type
             */
            public static final String TEXT_PLAIN = "text/plain";
            /**
             * application/json content-type
             */
            public static final String APPLICATION_JSON = "application/json";
            /**
             * text/html content-type
             */
            public static final String TEXT_HTML = "text/html";
            /**
             * text/xml content-type
             */
            public static final String APPLICATION_XML = "text/xml";

            private String raw;

            private final ObjectMapper objectMapper;

            public Raw(ObjectMapper objectMapper) {
                super.contentType = TEXT_PLAIN;
                this.objectMapper = objectMapper;
            }

            @Override
            protected ResponseReceiver<?> sender(RequestSender sender, Configurer configurer) {
                return sender.send((req, out) -> {
                    Optional.ofNullable(contentType).ifPresent(ct -> req.header(HttpHeaderNames.CONTENT_TYPE, ct));
                    return out.send(Mono.justOrEmpty(raw)
                        .map(s -> s.getBytes(configurer.charset))
                        .map(Unpooled::wrappedBuffer));
                });
            }

            /**
             * plain text raw request
             *
             * @param text plain
             * @return this
             */
            public Raw text(String text) {
                if (Objects.isNull(raw)) {
                    this.raw = text;
                    this.contentType = TEXT_PLAIN;
                }
                return this;
            }

            /**
             * json raw request
             *
             * @param text json format string
             * @return this
             */
            public Raw json(String text) {
                if (Objects.isNull(raw)) {
                    this.raw = text;
                    this.contentType = APPLICATION_JSON;
                }
                return this;
            }

            /**
             * json raw request
             *
             * @param obj object
             * @return this
             */
            public Raw json(Object obj) {
                if (Objects.isNull(raw) && Objects.nonNull(obj)) {
                    try {
                        this.raw = objectMapper.writeValueAsString(obj);
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException(e);
                    }
                    this.contentType = APPLICATION_JSON;
                }
                return this;
            }

            /**
             * html raw request
             *
             * @param text html
             * @return this
             */
            public Raw html(String text) {
                if (Objects.isNull(raw)) {
                    this.raw = text;
                    this.contentType = TEXT_HTML;
                }
                return this;
            }

            /**
             * xml raw request
             *
             * @param text xml
             * @return this
             */
            public Raw xml(String text) {
                if (Objects.isNull(raw)) {
                    this.raw = text;
                    this.contentType = APPLICATION_XML;
                }
                return this;
            }
        }

        /**
         * Binary payload body
         */
        public static class Binary extends AbstractBody<Binary> {

            private Publisher<? extends ByteBuf> byteBufPublisher;

            public Binary() {
                super.contentType = null;
            }

            @Override
            protected ResponseReceiver<?> sender(RequestSender sender, Configurer configurer) {
                if (Objects.nonNull(byteBufPublisher)) {
                    return sender.send((req, out) -> {
                        Optional.ofNullable(contentType).ifPresent(ct -> req.header(HttpHeaderNames.CONTENT_TYPE, ct));
                        return out.send(byteBufPublisher);
                    });
                }
                return sender;
            }

            /**
             * Publisher as data input
             *
             * @param publisher data
             * @return this
             */
            public Binary publisher(Publisher<? extends ByteBuf> publisher) {
                if (Objects.isNull(byteBufPublisher)) {
                    this.byteBufPublisher = publisher;
                }
                return this;
            }

            /**
             * Publisher as data input with specific content-type
             *
             * @param publisher   data
             * @param contentType content-type
             * @return this
             */
            public Binary publisher(Publisher<? extends ByteBuf> publisher, String contentType) {
                if (Objects.isNull(byteBufPublisher)) {
                    this.byteBufPublisher = publisher;
                }
                this.contentType = contentType;
                return this;
            }

            /**
             * File as data input
             *
             * @param file file
             * @return this
             */
            public Binary file(File file) {
                if (Objects.isNull(byteBufPublisher)) {
                    byteBufPublisher = Mono.fromSupplier(() -> {
                        try {
                            return Unpooled.wrappedBuffer(FileUtils.readFileToByteArray(file));
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    });
                }
                return this;
            }

            /**
             * Bytes array as data input
             *
             * @param bytes data
             * @return this
             */
            public Binary bytes(byte[] bytes) {
                if (Objects.isNull(byteBufPublisher)) {
                    byteBufPublisher = Mono.fromSupplier(() -> Unpooled.wrappedBuffer(bytes));
                }
                return this;
            }

            /**
             * InputStream as data input
             *
             * @param ips data
             * @return this
             */
            public Binary inputStream(InputStream ips) {
                if (Objects.isNull(byteBufPublisher)) {
                    byteBufPublisher = Mono.fromSupplier(() -> {
                        try {
                            return Unpooled.wrappedBuffer(IOUtils.toByteArray(ips));
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    });
                }
                return this;
            }

            /**
             * File as data input with specific content-type
             *
             * @param file        data
             * @param contentType content-type
             * @return this
             */
            public Binary file(File file, String contentType) {
                if (Objects.isNull(byteBufPublisher)) {
                    byteBufPublisher = Mono.fromSupplier(() -> {
                        try {
                            return Unpooled.wrappedBuffer(FileUtils.readFileToByteArray(file));
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    });
                    this.contentType = contentType;
                }
                return this;
            }

            /**
             * Bytes array as data input with specific content-type
             *
             * @param bytes       data
             * @param contentType content-type
             * @return this
             */
            public Binary bytes(byte[] bytes, String contentType) {
                if (Objects.isNull(byteBufPublisher)) {
                    byteBufPublisher = Mono.fromSupplier(() -> Unpooled.wrappedBuffer(bytes));
                    this.contentType = contentType;
                }
                return this;
            }

            /**
             * InputStream as data input with specific content-type
             *
             * @param ips         data
             * @param contentType content-type
             * @return this
             */
            public Binary inputStream(InputStream ips, String contentType) {
                if (Objects.isNull(byteBufPublisher)) {
                    byteBufPublisher = Mono.fromSupplier(() -> {
                        try {
                            return Unpooled.wrappedBuffer(IOUtils.toByteArray(ips));
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    });
                    this.contentType = contentType;
                }
                return this;
            }

        }

        /**
         * FormData payload body
         */
        public static class FormData extends AbstractBody<FormData> {

            /**
             * multipart/form-data content-type
             */
            public static final String MULTIPART_FORM_DATA = "multipart/form-data";

            private final List<Function<HttpClientForm, HttpClientForm>> parts = new ArrayList<>();

            public FormData() {
                super.contentType = null;
            }

            @Override
            protected ResponseReceiver<?> sender(RequestSender sender, Configurer configurer) {
                return sender.sendForm((req, form) -> {
                    Optional.ofNullable(contentType).ifPresent(ct -> req.header(HttpHeaderNames.CONTENT_TYPE, ct));
                    form.multipart(true).charset(configurer.charset);
                    for (Function<HttpClientForm, HttpClientForm> part : parts) {
                        part.apply(form);
                    }
                });
            }

            /**
             * Text data
             *
             * @param name  property name
             * @param value property value
             * @return this
             */
            public FormData text(String name, String value) {
                parts.add(form -> form.attr(name, value));
                return this;
            }

            /**
             * File data
             *
             * @param name property name
             * @param file property content
             * @return this
             */
            public FormData file(String name, File file) {
                return file(name, name, file, MULTIPART_FORM_DATA);
            }

            /**
             * Bytes array data
             *
             * @param name  property name
             * @param bytes property content
             * @return this
             */
            public FormData bytes(String name, byte[] bytes) {
                return bytes(name, name, bytes, MULTIPART_FORM_DATA);
            }

            /**
             * InputStream data
             *
             * @param name property name
             * @param ips  property content
             * @return this
             */
            public FormData inputStream(String name, InputStream ips) {
                return inputStream(name, name, ips, MULTIPART_FORM_DATA);
            }

            /**
             * File data
             *
             * @param name        property name
             * @param fileName    file name
             * @param file        property content
             * @param contentType file content-type
             * @return this
             */
            public FormData file(String name, String fileName, File file, String contentType) {
                parts.add(form -> form.file(name, fileName, file, contentType));
                return this;
            }

            /**
             * Bytes array data
             *
             * @param name        property name
             * @param fileName    file name
             * @param bytes       property content
             * @param contentType file content-type
             * @return this
             */
            public FormData bytes(String name, String fileName, byte[] bytes, String contentType) {
                parts.add(form -> form.file(name, fileName, new ByteArrayInputStream(bytes), contentType));
                return this;
            }

            /**
             * InputStream data
             *
             * @param name        property name
             * @param fileName    file name
             * @param ips         property content
             * @param contentType file content-type
             * @return this
             */
            public FormData inputStream(String name, String fileName, InputStream ips, String contentType) {
                parts.add(form -> form.file(name, fileName, ips, MULTIPART_FORM_DATA));
                return this;
            }

        }

        /**
         * Form url encoded payload body
         */
        public static class FormUrlEncoded extends AbstractBody<FormUrlEncoded> {

            /**
             * application/x-www-form-urlencoded content-type
             */
            public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

            private final List<Entry<String, String>> pairs = new ArrayList<>();

            public FormUrlEncoded() {
                super.contentType = APPLICATION_FORM_URLENCODED;
            }

            @Override
            protected ResponseReceiver<?> sender(RequestSender sender, Configurer configurer) {
                return sender.sendForm((req, form) -> {
                    Optional.ofNullable(contentType).ifPresent(ct -> req.header(HttpHeaderNames.CONTENT_TYPE, ct));
                    form.charset(configurer.charset).multipart(false);
                    for (Entry<String, String> pair : pairs) {
                        form.attr(pair.getKey(), pair.getValue());
                    }
                });
            }

            /**
             * Form url encoded property
             *
             * @param name  property name
             * @param value property value
             * @return this
             */
            public FormUrlEncoded text(String name, String value) {
                pairs.add(new AbstractMap.SimpleImmutableEntry<>(name, value));
                return this;
            }

        }
    }

    public static HttpResult blockSingle(ResponseReceiver<?> responseReceiver) {
        HttpResult httpResult = new HttpResult();
        long start = System.currentTimeMillis();
        Mono<byte[]> byteMono = responseReceiver.responseSingle((httpClientResponse, byteBufMono) -> {
            httpResult.setHttpClientResponse(httpClientResponse);
            httpResult.init();
            return byteBufMono.asByteArray();
        });
        byte[] content = byteMono.block();
        if (log.isDebugEnabled()) {
            log.debug("response: cost={}ms, code={}, length={}", System.currentTimeMillis() - start, httpResult.getStatusCode(), httpResult.contentLength);
        }
        httpResult.setContent(content);
        return httpResult;
    }

    /**
     * Blocking http result keeper
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HttpResult {

        @Getter
        private int statusCode;
        @Getter
        private String version;
        @Getter
        private String reasonPhrase;
        @Setter(AccessLevel.PRIVATE)
        private HttpClientResponse httpClientResponse;
        private final Map<String, List<String>> headers = new HashMap<>();
        @Setter(AccessLevel.PRIVATE)
        private String contentType;
        @Setter(AccessLevel.PRIVATE)
        private String contentLength;
        @Getter
        private Charset charset = StandardCharsets.UTF_8;
        @Setter(AccessLevel.PRIVATE)
        private byte[] content;
        @Getter
        @Setter(AccessLevel.PRIVATE)
        private Map<CharSequence, Set<Cookie>> cookies;

        private void init() {
            HttpResponseStatus status = httpClientResponse.status();
            this.version = httpClientResponse.version().text();
            this.statusCode = status.code();
            this.reasonPhrase = status.reasonPhrase();
            HttpHeaders entries = httpClientResponse.responseHeaders();
            setHeaders(entries);
            Optional.ofNullable(getHeader(HttpHeaderNames.CONTENT_TYPE.toString()))
                .ifPresent(contentType -> {
                    setContentType(contentType);
                    if (StringUtils.isNotBlank(contentType)) {
                        String[] split = contentType.split(";");
                        Arrays.stream(split).map(String::trim)
                            .filter(StringUtils::isNotBlank)
                            .filter(s -> StringUtils.startsWithIgnoreCase(s, "charset"))
                            .map(s -> StringUtils.removeStartIgnoreCase(s, "charset="))
                            .findFirst()
                            .ifPresent(this::setCharset);
                    }
                });
            Optional.ofNullable(getHeader(HttpHeaderNames.CONTENT_LENGTH.toString()))
                .ifPresent(HttpResult.this::setContentLength);
            cookies = httpClientResponse.cookies();
        }

        @Override
        public String toString() {
            return "HttpResult [statusCode=" + getStatusCode() + ", content-type=" + contentType + ", content-length="
                   + contentLength + "]";
        }

        public String getStatusLine() {
            return version + " " + statusCode + " " + reasonPhrase;
        }

        /**
         * Is OK
         *
         * @return status code is 2XX
         */
        public boolean isOK() {
            return 200 <= this.statusCode && this.statusCode < 300;
        }

        private void setCharset(String charset) {
            try {
                this.charset = Charset.forName(charset);
            } catch (Exception ignored) {
            }
        }

        /**
         * Get all http response headers
         *
         * @return response headers multi value map
         */
        public Map<String, List<String>> getAllHeaders() {
            return headers;
        }

        /**
         * Get first response header by name
         *
         * @param name header name
         * @return first result
         */
        public String getHeader(String name) {
            List<String> list = getAllHeaders().get(name);
            if (CollectionUtils.isNotEmpty(list)) {
                return list.get(0);
            }
            return null;
        }

        /**
         * Get all headers by name
         *
         * @param name header name
         * @return header values with name
         */
        public List<String> getHeaders(String name) {
            return headers.get(name);
        }

        private void setHeader(String name, String value) {
            List<String> list = this.headers.get(name);
            if (Objects.isNull(list)) {
                list = new ArrayList<>();
                this.headers.put(name, list);
            }
            list.add(value);
        }

        private void setHeader(Entry<String, String> header) {
            setHeader(header.getKey(), header.getValue());
        }

        private void setHeaders(HttpHeaders headers) {
            for (Entry<String, String> header : headers) {
                setHeader(header);
            }
        }

        /**
         * Get cookies by name
         *
         * @param name cookie name
         * @return cookies
         */
        public Set<Cookie> getCookie(String name) {
            return Optional.ofNullable(cookies).map(_map -> _map.get(name)).orElse(Collections.emptySet());
        }

        /**
         * Http response content
         *
         * @return content payload
         */
        public byte[] content() {
            return wrap(bytes -> bytes);
        }

        /**
         * Encode response content with charset
         *
         * @return plain response content
         */
        public String str() {
            return wrap(bytes -> new String(bytes, charset));
        }

        /**
         * Apply data resolver
         *
         * @param wrapper content resolver
         * @param <T>     resolved type
         * @return resolved value
         */
        public <T> T wrap(Function<byte[], T> wrapper) {
            Objects.requireNonNull(wrapper, "wrapper should not be null");
            return Optional.ofNullable(content).map(wrapper).orElse(null);
        }

    }

    private void _assertState(boolean state, String message) {
        if (!state) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Resolve HttpClientResponse charset
     *
     * @param httpClientResponse httpClientResponse
     * @return charset
     */
    public static Charset getCharset(HttpClientResponse httpClientResponse) {
        HttpHeaders entries = httpClientResponse.responseHeaders();
        return Optional.ofNullable(entries.get(HttpHeaderNames.CONTENT_TYPE.toString()))
            .flatMap(contentType -> {
                String[] split = contentType.split(";");
                return Arrays.stream(split).map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .filter(s -> StringUtils.startsWithIgnoreCase(s, "charset"))
                    .map(s -> StringUtils.removeStartIgnoreCase(s, "charset="))
                    .findFirst()
                    .map(Charset::forName);
            })
            .orElse(StandardCharsets.UTF_8);
    }

    @Getter
    public static final class FilterContext {
        private final ReactiveHttpUtils self;
        private final Configurer configurer;
        private final Map<String, Object> attributes;

        public FilterContext(ReactiveHttpUtils self, HttpClient httpClient, Configurer configurer) {
            this.self = self;
            this.httpClient = httpClient;
            this.configurer = configurer;
            this.attributes = new HashMap<>();
        }

        @Setter
        private HttpClient httpClient;
        @Setter
        private RequestSender requestSender;
        @Setter
        private ResponseReceiver<?> responseReceiver;

        @SuppressWarnings("unchecked")
        public <T> T get(String key) {
            return (T) attributes.get(key);
        }

        public <T> Optional<T> tryGet(String key) {
            T value = get(key);
            return Optional.ofNullable(value);
        }

        public void set(String key, Object value) {
            attributes.put(key, value);
        }

    }

    public enum Stage {
        FIRST(Integer.MIN_VALUE),
        SET_HEADERS(0),
        CREATE_SENDER(1000),
        SET_URI(2000),
        SET_BODY(3000),
        EXECUTE(Integer.MAX_VALUE), // Send request
        ;

        private final int order;

        Stage(int order) {
            this.order = order;
        }

        public int order() {
            return order;
        }
    }

    public interface Filter {
        Mono<HttpResult> filter(FilterChain chain, FilterContext ctx);
    }

    private static class SetHeadersFilter implements Filter {
        private static final SetHeadersFilter INSTANCE = new SetHeadersFilter();

        @Override
        public Mono<HttpResult> filter(FilterChain chain, FilterContext ctx) {
            ctx.httpClient = ctx.httpClient.headers(entries -> ctx.configurer.headers().forEach(entries::add));
            return chain.doFilter(ctx);
        }
    }

    private static class CreateSenderFilter implements Filter {
        private static final CreateSenderFilter INSTANCE = new CreateSenderFilter();

        @Override
        public Mono<HttpResult> filter(FilterChain chain, FilterContext ctx) {
            ctx.requestSender = ctx.httpClient.request(HttpMethod.valueOf(ctx.configurer.method()));
            return chain.doFilter(ctx);
        }
    }

    private static class SetUriFilter implements Filter {
        private static final SetUriFilter INSTANCE = new SetUriFilter(null);
        private final Function<String, URI> hostResolver;

        public SetUriFilter(Function<String, URI> hostResolver) {
            this.hostResolver = hostResolver;
        }

        @Override
        public Mono<HttpResult> filter(FilterChain chain, FilterContext ctx) {
            try {
                URIBuilder uriBuilder = new URIBuilder(ctx.configurer.url, ctx.configurer.charset);
                Optional.ofNullable(hostResolver)
                    .map(resolver -> resolver.apply(uriBuilder.getHost()))
                    .ifPresent(uri -> {
                        uriBuilder.setHost(uri.getHost());
                        uriBuilder.setPort(uri.getPort());
                    });
                URI uri = uriBuilder.addParameters(ctx.configurer.params).build();
                ctx.requestSender = ctx.requestSender.uri(uri);
                return chain.doFilter(ctx);
            } catch (URISyntaxException e) {
                return Mono.error(e);
            }
        }
    }

    private static class CreateReceiverFilter implements Filter {
        private static final CreateReceiverFilter INSTANCE = new CreateReceiverFilter();

        @Override
        public Mono<HttpResult> filter(FilterChain chain, FilterContext ctx) {
            ctx.responseReceiver = Optional.ofNullable(ctx.configurer.bodyConfigurer)
                .map(bodyBuilder -> {
                    Configurer.Payload payload = new Configurer.Payload(ctx.configurer);
                    bodyBuilder.accept(payload);
                    return payload.getBody();
                }).<ResponseReceiver<?>>map(b -> {
                    b.init();
                    return b.sender(ctx.requestSender, ctx.configurer);
                })
                .orElse(ctx.requestSender);
            return chain.doFilter(ctx);
        }
    }

    private static class ExecuteFilter implements Filter {
        private static final ExecuteFilter INSTANCE = new ExecuteFilter();

        @Override
        public Mono<HttpResult> filter(FilterChain chain, FilterContext ctx) {
            long startedAt = System.currentTimeMillis();
            return ctx.responseReceiver.responseSingle((resp, byteBuf) -> {
                HttpResult httpResult = new HttpResult();
                httpResult.httpClientResponse = resp;
                httpResult.init();
                return byteBuf.asByteArray()
                    .map(bytes -> {
                        httpResult.content = bytes;
                        return httpResult;
                    })
                    .doOnNext(unused -> ctx.set("elapsed", Duration.ofMillis(System.currentTimeMillis() - startedAt)));
            });
        }
    }

    public static final class FilterChain {
        private final Filter current;
        private final FilterChain chain;

        private FilterChain(Filter current, FilterChain chain) {
            this.current = current;
            this.chain = chain;
        }

        private static FilterChain create(List<Filter> filters) {
            FilterChain chain = new FilterChain(null, null);
            for (int i = filters.size() - 1; i >= 0; i--) {
                chain = new FilterChain(filters.get(i), chain);
            }
            return chain;
        }

        public Mono<HttpResult> doFilter(FilterContext ctx) {
            return current != null && chain != null
                ? current.filter(chain, ctx)
                : Mono.create(MonoSink::success);
        }

    }

    public static final class ChainBuilder {
        private final SortedMap<Integer, Filter> filters = new TreeMap<>();

        public ChainBuilder withDefault() {
            this.addFilterAt(Stage.SET_HEADERS, SetHeadersFilter.INSTANCE);
            this.addFilterAt(Stage.CREATE_SENDER, CreateSenderFilter.INSTANCE);
            this.addFilterAt(Stage.SET_URI, SetUriFilter.INSTANCE);
            this.addFilterAt(Stage.SET_BODY, CreateReceiverFilter.INSTANCE);
            this.addFilterAt(Stage.EXECUTE, ExecuteFilter.INSTANCE);
            return this;
        }

        public ChainBuilder useHostResolver(Function<String, URI> hostResolver) {
            this.addFilterAt(Stage.SET_URI, new SetUriFilter(hostResolver));
            return this;
        }

        public ChainBuilder addFilterAt(Stage stage, Filter filter) {
            filters.put(stage.order(), filter);
            return this;
        }

        public ChainBuilder addFilterAt(int stage, Filter filter) {
            filters.put(stage, filter);
            return this;
        }

        public ChainBuilder addFilterBefore(Stage stage, Filter filter) {
            if (stage == Stage.FIRST) {
                throw new IllegalArgumentException("Cannot add filter before FIRST");
            }
            int stageOrder = stage.order() - 1;
            while (filters.containsKey(stageOrder)) {
                stageOrder--;
            }
            filters.put(stageOrder, filter);
            return this;
        }

        public ChainBuilder addFilterAfter(Stage stage, Filter filter) {
            if (stage == Stage.EXECUTE) {
                throw new IllegalArgumentException("Cannot add filter after LAST");
            }
            int stageOrder = stage.order() + 1;
            while (filters.containsKey(stageOrder)) {
                stageOrder++;
            }
            filters.put(stageOrder, filter);
            return this;
        }

        private FilterChain build() {
            return FilterChain.create(new ArrayList<>(filters.values()));
        }
    }
}
