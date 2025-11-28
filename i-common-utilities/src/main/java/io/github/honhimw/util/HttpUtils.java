package io.github.honhimw.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.net.WWWFormCodec;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * apache httpclient5 utils
 *
 * @author hon_him
 * @since 2023-07-13
 */

@Slf4j
@SuppressWarnings({
    "unused",
    "UnusedReturnValue",
})
public class HttpUtils {

    private HttpUtils() {
    }

    /**
     * Max total connections
     */
    public static final int MAX_TOTAL_CONNECTIONS = 1_000;

    /**
     * Max connections per route
     */
    public static final int MAX_ROUTE_CONNECTIONS = 200;

    /**
     * Connect timeout
     */
    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(4);

    /**
     * Read timeout
     */
    public static final Duration READ_TIMEOUT = Duration.ofSeconds(20);

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final byte[] EMPTY_DATA = new byte[0];

    private static HttpUtils INSTANCE;

    private PoolingHttpClientConnectionManager connectionManager;

    private CloseableHttpClient httpClient;

    @Getter
    private ObjectMapper objectMapper;

    @Getter
    private RequestConfig defaultRequestConfig;

    private ChainBuilder chainBuilder;

    private void init(InitCustomizer customizer) {
        SocketConfig.Builder socketConfigBuilder = SocketConfig.custom()
            .setSoTimeout(1, TimeUnit.MINUTES)
            .setSoKeepAlive(true);
        ConnectionConfig.Builder connectionConfigBuilder = ConnectionConfig.custom()
            .setConnectTimeout(Timeout.of(CONNECT_TIMEOUT))
            .setSocketTimeout(Timeout.of(READ_TIMEOUT))
            .setTimeToLive(10, TimeUnit.MINUTES);
        PoolingHttpClientConnectionManagerBuilder poolingHttpClientConnectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();
        poolingHttpClientConnectionManagerBuilder
            .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
            .setConnPoolPolicy(PoolReusePolicy.LIFO)
            .setMaxConnTotal(MAX_TOTAL_CONNECTIONS);
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
            .setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.ofSeconds(1)));
        objectMapper = JsonUtils.mapper().copy();
        chainBuilder = new ChainBuilder().withDefault();
        Initializer initializer = new Initializer(
            socketConfigBuilder,
            connectionConfigBuilder,
            poolingHttpClientConnectionManagerBuilder,
            httpClientBuilder,
            requestConfigBuilder,
            objectMapper,
            chainBuilder
        );
        customizer.customize(initializer);
        SSLContext sslContext;
        try {
            sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                .build();
        } catch (Exception e) {
            sslContext = SSLContexts.createDefault();
        }

        connectionManager = poolingHttpClientConnectionManagerBuilder
            .setTlsSocketStrategy(new DefaultClientTlsStrategy(
                sslContext,
                HostnameVerificationPolicy.CLIENT,
                NoopHostnameVerifier.INSTANCE))
            .setDefaultSocketConfig(socketConfigBuilder.build())
            .setDefaultConnectionConfig(connectionConfigBuilder.build())
            .setMaxConnPerRoute(MAX_ROUTE_CONNECTIONS)
            .build();
        defaultRequestConfig = requestConfigBuilder.build();
        httpClientBuilder
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(defaultRequestConfig);
        httpClient = httpClientBuilder.build();
    }

    /**
     * Get shared instance, initialize with default settings
     *
     * @return shared instance
     * @see #buildSharedInstance(InitCustomizer)
     */
    public static HttpUtils getSharedInstance() {
        if (Objects.isNull(INSTANCE)) {
            buildSharedInstance(customizer -> {
            });
        }
        return INSTANCE;
    }

    public static void buildSharedInstance(InitCustomizer initializer) {
        INSTANCE = newInstance(initializer);
    }

    public static HttpUtils newInstance() {
        return newInstance(customizer -> {
        });
    }

    public static HttpUtils newInstance(InitCustomizer initializer) {
        HttpUtils http5Utils = new HttpUtils();
        http5Utils.init(initializer);
        return http5Utils;
    }

    /**
     * Set max connections for given route
     *
     * @param route url/uri
     * @param max   max route connections
     */
    public void setMaxPerRoute(String route, int max) {
        Optional.ofNullable(connectionManager)
            .ifPresent(poolingHttpClientConnectionManager -> {
                URI uri = URI.create(route);
                HttpRoute httpRoute = new HttpRoute(new HttpHost(uri.getScheme(), uri.getHost(), uri.getPort()));
                poolingHttpClientConnectionManager.setMaxPerRoute(httpRoute, max);
            });
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper);
        this.objectMapper = objectMapper;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public HttpResult request(Consumer<Configurer> configurer) throws URISyntaxException, IOException {
        return request(null, null, configurer, httpResult -> httpResult);
    }

    public HttpResult request(String method, String url, Consumer<Configurer> configurer) throws URISyntaxException, IOException {
        return request(method, url, configurer, httpResult -> httpResult);
    }

    public <T> T request(String method, String url, Consumer<Configurer> configurer, Function<HttpResult, T> resultMapper) throws URISyntaxException, IOException {
        _assertState(Objects.nonNull(configurer), "String should not be null");
        Consumer<Configurer> _configurer = conf -> conf
            .method(method)
            .charset(DEFAULT_CHARSET)
            .url(url)
            .config(RequestConfig.copy(defaultRequestConfig).build());
        HttpResult httpResult = execute(_configurer.andThen(configurer));
        return resultMapper.apply(httpResult);
    }

    public HttpResult get(String url) throws URISyntaxException, IOException {
        return request(HttpGet.METHOD_NAME, url, configurer -> {
        });
    }

    public HttpResult post(String url) throws URISyntaxException, IOException {
        return request(HttpPost.METHOD_NAME, url, configurer -> {
        });
    }

    public HttpResult put(String url) throws URISyntaxException, IOException {
        return request(HttpPut.METHOD_NAME, url, configurer -> {
        });
    }

    public HttpResult delete(String url) throws URISyntaxException, IOException {
        return request(HttpDelete.METHOD_NAME, url, configurer -> {
        });
    }

    public HttpResult get(String url, Consumer<Configurer> configurer) throws URISyntaxException, IOException {
        return request(HttpGet.METHOD_NAME, url, configurer);
    }

    public HttpResult post(String url, Consumer<Configurer> configurer) throws URISyntaxException, IOException {
        return request(HttpPost.METHOD_NAME, url, configurer);
    }

    public HttpResult put(String url, Consumer<Configurer> configurer) throws URISyntaxException, IOException {
        return request(HttpPut.METHOD_NAME, url, configurer);
    }

    public HttpResult delete(String url, Consumer<Configurer> configurer) throws URISyntaxException, IOException {
        return request(HttpDelete.METHOD_NAME, url, configurer);
    }

    public <T> T get(String url, Consumer<Configurer> configurer, Function<HttpResult, T> resultMapper) throws URISyntaxException, IOException {
        return request(HttpGet.METHOD_NAME, url, configurer, resultMapper);
    }

    public <T> T post(String url, Consumer<Configurer> configurer, Function<HttpResult, T> resultMapper) throws URISyntaxException, IOException {
        return request(HttpPost.METHOD_NAME, url, configurer, resultMapper);
    }

    public <T> T put(String url, Consumer<Configurer> configurer, Function<HttpResult, T> resultMapper) throws URISyntaxException, IOException {
        return request(HttpPut.METHOD_NAME, url, configurer, resultMapper);
    }

    public <T> T delete(String url, Consumer<Configurer> configurer, Function<HttpResult, T> resultMapper) throws URISyntaxException, IOException {
        return request(HttpDelete.METHOD_NAME, url, configurer, resultMapper);
    }

    public HttpResult execute(Consumer<Configurer> configurer) throws IOException {
        Configurer requestConfigurer = new Configurer(this);
        configurer.accept(requestConfigurer);
        _assertState(StringUtils.isNotBlank(requestConfigurer.getMethod()), "Method should not be blank");
        _assertState(StringUtils.isNotBlank(requestConfigurer.getUrl()), "URL should not be blank");
        FilterContext ctx = new FilterContext(this, httpClient, requestConfigurer);
        try {
            FilterChain filterChain = chainBuilder.build();
            filterChain.doFilter(ctx);
            if (filterChain.pos < filterChain.filters.size()) {
                log.warn("The filter chain did not execute completely. Terminated at {}", filterChain.filters.get(filterChain.pos - 1));
            }
            return ctx.httpResult;
        } catch (Exception e) {
            throw new IOException("HTTP Execution Error", e);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Initializer {

        private SocketConfig.Builder socketConfigBuilder;

        private ConnectionConfig.Builder connectionConfigBuilder;

        private PoolingHttpClientConnectionManagerBuilder poolingHttpClientConnectionManagerBuilder;

        private HttpClientBuilder httpClientBuilder;

        private RequestConfig.Builder requestConfigBuilder;

        private ObjectMapper objectMapper;

        private ChainBuilder chainBuilder;

        public Initializer socket(Consumer<SocketConfig.Builder> customizer) {
            Optional.ofNullable(socketConfigBuilder).ifPresent(customizer);
            return this;
        }

        public Initializer connection(Consumer<ConnectionConfig.Builder> customizer) {
            Optional.ofNullable(connectionConfigBuilder).ifPresent(customizer);
            return this;
        }

        public Initializer connectionManager(Consumer<PoolingHttpClientConnectionManagerBuilder> customizer) {
            Optional.ofNullable(poolingHttpClientConnectionManagerBuilder).ifPresent(customizer);
            return this;
        }

        public Initializer client(Consumer<HttpClientBuilder> customizer) {
            Optional.ofNullable(httpClientBuilder).ifPresent(customizer);
            return this;
        }

        public Initializer request(Consumer<RequestConfig.Builder> customizer) {
            Optional.ofNullable(requestConfigBuilder).ifPresent(customizer);
            return this;
        }

        public Initializer mapper(Consumer<ObjectMapper> customizer) {
            Optional.ofNullable(objectMapper).ifPresent(customizer);
            return this;
        }

        public Initializer filter(Consumer<ChainBuilder> customizer) {
            Optional.ofNullable(chainBuilder).ifPresent(customizer);
            return this;
        }
    }

    @FunctionalInterface
    public interface InitCustomizer {
        void customize(Initializer customizer);
    }

    public final static class Configurer {

        private final HttpUtils self;

        private Configurer(HttpUtils self) {
            this.self = self;
        }

        @Getter
        private String method;

        @Getter
        private Charset charset;

        @Getter
        private String url;

        @Getter
        private final Map<String, List<String>> headers = new LinkedHashMap<>();

        @Getter
        private final List<NameValuePair> params = new ArrayList<>();

        @Getter
        private HttpEntity httpEntity;

        private HttpContext context;

        @Getter
        private RequestConfig config;

        private Consumer<InputStream> inputConfig;

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
            this.method = HttpGet.METHOD_NAME;
            return this;
        }

        /**
         * POST
         *
         * @return this
         */
        public Configurer post() {
            this.method = HttpPost.METHOD_NAME;
            return this;
        }

        /**
         * PUT
         *
         * @return this
         */
        public Configurer put() {
            this.method = HttpPut.METHOD_NAME;
            return this;
        }

        /**
         * DELETE
         *
         * @return this
         */
        public Configurer delete() {
            this.method = HttpDelete.METHOD_NAME;
            return this;
        }

        /**
         * PATCH
         *
         * @return this
         */
        public Configurer patch() {
            this.method = HttpPatch.METHOD_NAME;
            return this;
        }

        public Configurer charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Configurer url(String url) {
            this.url = url;
            return this;
        }

        public Configurer header(String name, String value) {
            List<String> list = this.headers.get(name);
            if (Objects.isNull(list)) {
                list = new ArrayList<>();
                this.headers.put(name, list);
            }
            list.add(value);
            return this;
        }

        public Configurer headers(Map<String, String> headers) {
            headers.forEach(this::header);
            return this;
        }

        public Configurer param(String name, String value) {
            BasicNameValuePair basicNameValuePair = new BasicNameValuePair(name, value);
            params.add(basicNameValuePair);
            return this;
        }

        public Configurer params(Map<String, String> headers) {
            headers.forEach(this::param);
            return this;
        }

        public Configurer config(RequestConfig config) {
            this.config = config;
            return this;
        }

        public Configurer config(Consumer<RequestConfig.Builder> consumer) {
            RequestConfig.Builder copy = RequestConfig.copy(config);
            consumer.accept(copy);
            this.config = copy.build();
            return this;
        }

        public Configurer context(HttpContext context) {
            this.context = context;
            return this;
        }

        public Configurer body(Consumer<Body> configurer) {
            Body bodyModel = new Body(this);
            configurer.accept(bodyModel);
            AbstractBody<?> body = bodyModel.getBody();
            if (Objects.nonNull(body)) {
                httpEntity = body.toEntity(this.charset);
            }
            return this;
        }

        /**
         * Alias for most usage cases
         *
         * @param json json format raw text
         * @return this chain
         */
        public Configurer json(String json) {
            return body(body -> body.raw(raw -> raw.json(json)));
        }

        /**
         * Alias for most usage cases
         *
         * @param obj to be json encoded object
         * @return this chain
         */
        public Configurer json(Object obj) {
            return body(body -> body.raw(raw -> raw.json(obj)));
        }

        private HttpClientContext getContext() {
            HttpClientContext ctx;
            if (Objects.isNull(context)) {
                ctx = HttpClientContext.create();
                ctx.setCookieStore(new BasicCookieStore());
            } else if (!(context instanceof HttpClientContext)) {
                ctx = HttpClientContext.castOrCreate(context);
                ctx.setCookieStore(new BasicCookieStore());
            } else {
                ctx = (HttpClientContext) context;
            }
            return ctx;
        }

        public Configurer result(Consumer<InputStream> configurer) {
            inputConfig = configurer;
            return this;
        }

        public static class Body {
            private final Configurer self;

            private Body(Configurer self) {
                this.self = self;
            }

            private AbstractBody<?> body;

            protected AbstractBody<?> getBody() {
                return body;
            }

            public Body raw(Consumer<Raw> configurer) {
                return type(() -> new Raw(self.self.objectMapper), configurer);
            }

            public Body formData(Consumer<FormData> configurer) {
                return type(() -> new FormData(self.charset), configurer);
            }

            public Body binary(Consumer<Binary> configurer) {
                return type(Binary::new, configurer);
            }

            public Body formUrlEncoded(Consumer<FormUrlEncoded> configurer) {
                return type(FormUrlEncoded::new, configurer);
            }

            public <T extends AbstractBody<T>> Body type(Supplier<T> buildable, Consumer<T> configurer) {
                Objects.requireNonNull(buildable);
                Objects.requireNonNull(configurer);
                if (Objects.isNull(body)) {
                    T built = buildable.get();
                    if (Objects.nonNull(built)) {
                        built.init();
                        configurer.accept(built);
                        body = built;
                    }
                }
                return this;
            }
        }


        public static abstract class AbstractBody<I extends AbstractBody<I>> {

            protected ContentType contentType;

            private boolean withCharset = false;

            protected void init() {

            }

            /**
             * Content type with or without charset. default: false
             * For example:
             * with: "application/json; charset=utf-8"
             * without: "application/json"
             *
             * @param withCharset true for with charset, false for without
             * @return this
             */
            @SuppressWarnings("unchecked")
            public I withCharset(boolean withCharset) {
                this.withCharset = withCharset;
                return (I) this;
            }

            /**
             * Most of the time you won't need to set the content type manually.
             * If you do need to set the content type manually, call this method after the body is built.
             * Because the content type is set automatically while building the body.
             *
             * @param contentType custom content type
             * @return this
             */
            @SuppressWarnings("unchecked")
            public I contentType(@Nullable ContentType contentType) {
                this.contentType = contentType;
                return (I) this;
            }

            protected String contentType() {
                return Optional.ofNullable(contentType)
                    .map(ct -> withCharset ? ct.toString() : ct.getMimeType())
                    .orElse(null);
            }

            protected abstract HttpEntity toEntity(Charset charset);
        }


        public static class Raw extends AbstractBody<Raw> {

            private final ObjectMapper objectMapper;
            private String raw;

            public Raw(ObjectMapper objectMapper) {
                this.objectMapper = objectMapper;
                super.contentType = ContentType.TEXT_PLAIN;
            }

            @Override
            protected HttpEntity toEntity(Charset charset) {
                if (Objects.nonNull(contentType)) {
                    contentType = contentType.withCharset(charset);
                }
                return new StringEntity(raw, contentType);
            }

            public Raw text(String text) {
                if (Objects.isNull(raw)) {
                    this.raw = text;
                    this.contentType = ContentType.TEXT_PLAIN;
                }
                return this;
            }

            public Raw json(String text) {
                if (Objects.isNull(raw)) {
                    this.raw = text;
                    this.contentType = ContentType.APPLICATION_JSON;
                }
                return this;
            }

            public Raw json(Object obj) {
                if (Objects.isNull(raw) && Objects.nonNull(obj)) {
                    try {
                        this.raw = objectMapper.writeValueAsString(obj);
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException(e);
                    }
                    this.contentType = ContentType.APPLICATION_JSON;
                }
                return this;
            }

            public Raw html(String text) {
                if (Objects.isNull(raw)) {
                    this.raw = text;
                    this.contentType = ContentType.TEXT_HTML;
                }
                return this;
            }

            public Raw xml(String text) {
                if (Objects.isNull(raw)) {
                    this.raw = text;
                    this.contentType = ContentType.APPLICATION_XML;
                }
                return this;
            }
        }

        public static class Binary extends AbstractBody<Binary> {

            private ContentType contentType;

            private Supplier<byte[]> bytesSupplier;

            @Override
            protected HttpEntity toEntity(Charset charset) {
                return Optional.ofNullable(bytesSupplier)
                    .map(Supplier::get)
                    .map(bytes -> new ByteArrayEntity(bytes, 0, bytes.length, contentType))
                    .orElse(null);
            }

            public Binary file(File file) {
                if (Objects.isNull(bytesSupplier)) {
                    bytesSupplier = () -> {
                        try {
                            return FileUtils.readFileToByteArray(file);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    };
                }
                return this;
            }

            public Binary bytes(byte[] bytes) {
                if (Objects.isNull(bytesSupplier)) {
                    this.bytesSupplier = () -> bytes;
                }
                return this;
            }

            public Binary inputStream(InputStream ips) {
                if (Objects.isNull(bytesSupplier)) {
                    this.bytesSupplier = () -> {
                        try {
                            return ips.readAllBytes();
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    };
                }
                return this;
            }

            public Binary file(File file, ContentType contentType) {
                if (Objects.isNull(bytesSupplier)) {
                    bytesSupplier = () -> {
                        try {
                            return FileUtils.readFileToByteArray(file);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    };
                    this.contentType = contentType;
                }
                return this;
            }

            public Binary bytes(byte[] bytes, ContentType contentType) {
                if (Objects.isNull(bytesSupplier)) {
                    this.bytesSupplier = () -> bytes;
                    this.contentType = contentType;
                }
                return this;
            }

            public Binary inputStream(InputStream ips, ContentType contentType) {
                if (Objects.isNull(bytesSupplier)) {
                    this.bytesSupplier = () -> {
                        try {
                            return ips.readAllBytes();
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    };
                    this.contentType = contentType;
                }
                return this;
            }

        }

        public static class FormData extends AbstractBody<FormData> {

            public static final ContentType MULTIPART_FORM_DATA = ContentType.MULTIPART_FORM_DATA;

            private final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            private final Charset charset;

            public FormData(Charset charset) {
                this.charset = charset;
                super.contentType = ContentType.MULTIPART_FORM_DATA.withCharset(charset);
            }

            @Override
            protected HttpEntity toEntity(Charset charset) {
                return builder.setContentType(contentType).setCharset(charset).build();
            }

            public FormData text(String name, String value) {
                builder.addTextBody(name, value, ContentType.DEFAULT_TEXT.withCharset(charset));
                return this;
            }

            public FormData file(String name, File file) {
                builder.addBinaryBody(name, file, ContentType.DEFAULT_BINARY.withCharset(charset), file.getName());
                return this;
            }

            public FormData bytes(String name, byte[] bytes) {
                builder.addBinaryBody(name, bytes, ContentType.DEFAULT_BINARY.withCharset(charset), name);
                return this;
            }

            public FormData inputStream(String name, InputStream ips) {
                builder.addBinaryBody(name, ips, ContentType.DEFAULT_BINARY.withCharset(charset), name);
                return this;
            }

            public FormData text(String name, String value, ContentType contentType) {
                builder.addTextBody(name, value, contentType);
                return this;
            }

            public FormData file(String name, File file, ContentType contentType, String fileName) {
                builder.addBinaryBody(name, file, contentType, fileName);
                return this;
            }

            public FormData bytes(String name, byte[] bytes, ContentType contentType, String fileName) {
                builder.addBinaryBody(name, bytes, contentType, fileName);
                return this;
            }

            public FormData inputStream(String name, InputStream ips, ContentType contentType, String fileName) {
                builder.addBinaryBody(name, ips, contentType, fileName);
                return this;
            }

        }

        public static class FormUrlEncoded extends AbstractBody<FormUrlEncoded> {

            public static final ContentType APPLICATION_FORM_URLENCODED = ContentType.APPLICATION_FORM_URLENCODED;

            private final List<NameValuePair> nameValuePairs = new ArrayList<>();

            public FormUrlEncoded() {
                super.contentType = ContentType.APPLICATION_FORM_URLENCODED;
            }

            @Override
            protected HttpEntity toEntity(Charset charset) {
                if (Objects.nonNull(contentType)) {
                    contentType = contentType.withCharset(charset);
                }
                return new StringEntity(WWWFormCodec.format(nameValuePairs, charset), contentType);
            }

            public FormUrlEncoded text(String name, String value) {
                BasicNameValuePair basicNameValuePair = new BasicNameValuePair(name, value);
                nameValuePairs.add(basicNameValuePair);
                return this;
            }

        }
    }

    @Getter
    public static final class FilterContext {
        private final HttpUtils self;
        private final CloseableHttpClient httpClient;
        private final Configurer configurer;
        private final Map<String, Object> attributes;

        private FilterContext(HttpUtils self, CloseableHttpClient httpClient, Configurer configurer) {
            this.self = self;
            this.httpClient = httpClient;
            this.configurer = configurer;
            this.attributes = new LinkedHashMap<>();
        }

        @Setter
        private ClassicRequestBuilder requestBuilder;
        private HttpClientContext httpContext;
        private ClassicHttpRequest httpRequest;
        private ClassicHttpResponse httpResponse;
        private HttpResult httpResult;

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
        CREATE_REQUEST(0),
        SET_URI(1000),
        SET_PARAMS(2000),
        SET_ENTITY(3000),
        SET_HEADERS(4000),
        BUILD_REQUEST(5000),
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
        void filter(FilterChain chain, FilterContext ctx) throws Exception;
    }

    public static abstract class AbstractFilter implements Filter {
        @Override
        public final void filter(FilterChain chain, FilterContext ctx) throws Exception {
            preFilter(ctx);
            chain.doFilter(ctx);
            postFilter(ctx);
        }

        protected void preFilter(FilterContext ctx) throws Exception {
        }

        protected void postFilter(FilterContext ctx) throws Exception {
        }
    }

    private static class CreateRequestFilter extends AbstractFilter {
        private static final CreateRequestFilter INSTANCE = new CreateRequestFilter();

        @Override
        protected void preFilter(FilterContext ctx) throws Exception {
            ctx.requestBuilder = ClassicRequestBuilder.create(ctx.configurer.method);
        }
    }

    private static class SetUriFilter extends AbstractFilter {
        private static final SetUriFilter INSTANCE = new SetUriFilter(null);

        private final Function<String, URI> hostResolver;

        private SetUriFilter(Function<String, URI> hostResolver) {
            this.hostResolver = hostResolver;
        }

        @Override
        protected void preFilter(FilterContext ctx) throws Exception {
            URIBuilder uriBuilder = new URIBuilder(ctx.configurer.url, ctx.configurer.charset);
            Optional.ofNullable(hostResolver)
                .map(resolver -> resolver.apply(uriBuilder.getHost()))
                .ifPresent(uri -> {
                    uriBuilder.setHost(uri.getHost());
                    uriBuilder.setPort(uri.getPort());
                });
            URI uri = uriBuilder.build();
            ctx.requestBuilder.setUri(uri);
        }
    }

    private static class SetParamsFilter extends AbstractFilter {
        private static final SetParamsFilter INSTANCE = new SetParamsFilter();

        @Override
        public void preFilter(FilterContext ctx) throws Exception {
            ctx.configurer.params.forEach(ctx.requestBuilder::addParameter);
        }
    }

    private static class SetEntityFilter extends AbstractFilter {
        private static final SetEntityFilter INSTANCE = new SetEntityFilter();

        @Override
        public void preFilter(FilterContext ctx) throws Exception {
            if (Strings.CI.equalsAny(ctx.requestBuilder.getMethod(), HttpPost.METHOD_NAME, HttpPut.METHOD_NAME) && Objects.nonNull(ctx.configurer.httpEntity)) {
                ctx.requestBuilder.setEntity(ctx.configurer.httpEntity);
            }
        }
    }

    private static class SetHeadersFilter extends AbstractFilter {
        private static final SetHeadersFilter INSTANCE = new SetHeadersFilter();

        @Override
        public void preFilter(FilterContext ctx) throws Exception {
            if (MapUtils.isNotEmpty(ctx.configurer.headers)) {
                ctx.configurer.headers.forEach((name, values) -> values.forEach(value -> ctx.requestBuilder.addHeader(name, value)));
            }
        }
    }

    private static class BuildRequestFilter extends AbstractFilter {
        private static final BuildRequestFilter INSTANCE = new BuildRequestFilter();

        @Override
        public void preFilter(FilterContext ctx) throws Exception {
            ctx.httpContext = ctx.configurer.getContext();
            if (Objects.nonNull(ctx.configurer.config)) {
                ctx.httpContext.setRequestConfig(ctx.configurer.config);
            }
            ctx.httpRequest = ctx.requestBuilder.build();
        }
    }

    private static class ExecuteFilter implements Filter {
        private static final ExecuteFilter INSTANCE = new ExecuteFilter();

        @Override
        public void filter(FilterChain chain, FilterContext ctx) throws Exception {
            long startedAt = System.currentTimeMillis();
            ctx.httpResult = ctx.httpClient.execute(ctx.httpRequest, ctx.httpContext, response -> {
                HttpResult result = new HttpResult(ctx);
                Optional.ofNullable(ctx.httpContext)
                    .map(HttpClientContext::getCookieStore)
                    .ifPresent(result::setCookieStore);
                result.setStatusCode(response.getCode());
                result.setVersion(String.valueOf(response.getVersion()));
                result.setReasonPhrase(response.getReasonPhrase());
                result.setHeaders(response.getHeaders());

                if (Objects.nonNull(response.getEntity())) {
                    HttpEntity entity = response.getEntity();
                    result.setEntity(entity);
                    Optional.ofNullable(ctx.configurer.inputConfig)
                        .ifPresent(result::setInputConfig);
                    result.read();
                }
                ctx.httpResponse = response;
                return result;
            });
            ctx.httpResult.elapsed = Duration.ofMillis(System.currentTimeMillis() - startedAt);
        }
    }

    public static final class FilterChain {
        private final List<Filter> filters;
        private int pos;

        private FilterChain(List<Filter> filters) {
            this.filters = filters;
            this.pos = 0;
        }

        public void doFilter(FilterContext ctx) throws Exception {
            if (pos < filters.size()) {
                Filter filter = filters.get(pos++);
                filter.filter(this, ctx);
            }
        }
    }

    public static final class ChainBuilder {
        private final SortedMap<Integer, Filter> filters = new TreeMap<>();

        public ChainBuilder withDefault() {
            this.addFilterAt(Stage.CREATE_REQUEST, CreateRequestFilter.INSTANCE);
            this.addFilterAt(Stage.SET_URI, SetUriFilter.INSTANCE);
            this.addFilterAt(Stage.SET_PARAMS, SetParamsFilter.INSTANCE);
            this.addFilterAt(Stage.SET_ENTITY, SetEntityFilter.INSTANCE);
            this.addFilterAt(Stage.SET_HEADERS, SetHeadersFilter.INSTANCE);
            this.addFilterAt(Stage.BUILD_REQUEST, BuildRequestFilter.INSTANCE);
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
            return new FilterChain(new ArrayList<>(filters.values()));
        }
    }

    public static class HttpResult {
        @Getter
        private final FilterContext filterContext;

        private HttpResult(FilterContext filterContext) {
            this.filterContext = filterContext;
        }

        @Getter
        private int statusCode;
        private String version;
        private String reasonPhrase;
        @Getter
        private HttpEntity entity;
        private final Map<String, List<String>> headers = new LinkedHashMap<>();
        @Getter
        private Charset charset = StandardCharsets.UTF_8;
        private byte[] content = EMPTY_DATA;
        private CookieStore cookieStore;
        @Getter
        private Duration elapsed = Duration.ZERO;

        private Consumer<InputStream> inputConfig = inputStream -> {
            ByteArrayOutputStream baops = new ByteArrayOutputStream();
            try {
                IOUtils.copy(inputStream, baops);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            content = baops.toByteArray();
        };

        @Override
        public String toString() {
            return "HttpResult [statusCode=" + statusCode + ", entity=" + entity + "]";
        }

        public boolean isOK() {
            return 200 <= this.statusCode && this.statusCode < 300;
        }

        public String getStatusLine() {
            return version + " " + statusCode + " " + reasonPhrase;
        }

        private void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        private void setVersion(String version) {
            this.version = version;
        }

        private void setReasonPhrase(String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
        }

        private void setCharset(Charset charset) {
            this.charset = charset;
        }

        private void setEntity(HttpEntity entity) {
            this.entity = entity;
        }

        private void setInputConfig(Consumer<InputStream> inputConfig) {
            this.inputConfig = inputConfig;
        }

        private void setCookieStore(CookieStore cookieStore) {
            this.cookieStore = cookieStore;
        }

        private void read() throws IOException {
            Optional.ofNullable(this.entity)
                .map(EntityDetails::getContentType)
                .map(ContentType::parse)
                .map(ContentType::getCharset)
                .ifPresent(this::setCharset);
            if (Objects.nonNull(entity)) {
                InputStream ips = entity.getContent();
                inputConfig.accept(ips);
            }
        }

        public Map<String, List<String>> getAllHeaders() {
            return headers;
        }

        @NonNull
        public Optional<String> getHeader(String name) {
            Stream<String> values = getHeaderCaseInsensitive(name);
            return values.findFirst();
        }

        @NonNull
        public List<String> getHeaders(String name) {
            return getHeaderCaseInsensitive(name).toList();
        }

        private Stream<String> getHeaderCaseInsensitive(String name) {
            Stream<String> stream = Stream.empty();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                if (Strings.CI.equals(key, name)) {
                    stream = Stream.concat(stream, entry.getValue().stream());
                }
            }
            return stream;
        }

        private void setHeader(String name, String value) {
            List<String> list = this.headers.get(name);
            if (Objects.isNull(list)) {
                list = new ArrayList<>();
                this.headers.put(name, list);
            }
            list.add(value);
        }

        private void setHeader(Header header) {
            setHeader(header.getName(), header.getValue());
        }

        private void setHeaders(Header[] headers) {
            for (Header header : headers) {
                setHeader(header.getName(), header.getValue());
            }
        }

        public List<Cookie> getCookies() {
            return cookieStore.getCookies();
        }

        public List<Cookie> getCookie(String name) {
            return cookieStore.getCookies().stream().filter(cookie -> Strings.CS.equals(cookie.getName(), name)).toList();
        }

        public byte[] content() {
            return wrap(bytes -> bytes);
        }

        public String str() {
            return wrap(bytes -> new String(bytes, charset));
        }

        public <T> T json(Class<T> type) {
            return wrap(bytes -> {
                try {
                    return filterContext.getSelf().objectMapper.readValue(bytes, type);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Nullable
        public <T> T wrap(Function<byte[], T> wrapper) {
            Objects.requireNonNull(wrapper, "wrapper should not be null");
            return Optional.ofNullable(content).map(wrapper).orElse(null);
        }

        public void debug() {
            log.info(this.getStatusLine());
            this.getAllHeaders().forEach((k, values) -> values
                .forEach(v -> log.info("{}: {}", k, v)));
            getHeader(HttpHeaders.CONTENT_TYPE).ifPresent(contentType -> {
                MediaType parse = MediaType.parse(contentType);
                if (parse.isRawType()) {
                    log.info("{}", this.str());
                }
            });
        }

    }

    private void _assertState(boolean state, String message) {
        if (!state) {
            throw new IllegalStateException(message);
        }
    }

}
