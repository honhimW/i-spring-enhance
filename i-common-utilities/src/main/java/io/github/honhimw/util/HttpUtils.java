package io.github.honhimw.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

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

    private static final Charset defaultCharset = StandardCharsets.UTF_8;

    private static HttpUtils instance;

    private static PoolingHttpClientConnectionManager connectionManager;

    private static CloseableHttpClient httpClient;

    private static ObjectMapper OBJECT_MAPPER;

    @Getter
    private RequestConfig defaultRequestConfig;

    private Function<String, URI> loadBalancedResolver;

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
        OBJECT_MAPPER = JsonUtils.mapper().copy();
        Initializer initializer = new Initializer(socketConfigBuilder, connectionConfigBuilder, poolingHttpClientConnectionManagerBuilder, httpClientBuilder, requestConfigBuilder, OBJECT_MAPPER);
        customizer.customize(initializer);
        connectionManager = poolingHttpClientConnectionManagerBuilder
            .setTlsSocketStrategy(DefaultClientTlsStrategy.createDefault())
            .setDefaultSocketConfig(socketConfigBuilder.build())
            .setDefaultConnectionConfig(connectionConfigBuilder.build())
            .build();
        defaultRequestConfig = requestConfigBuilder.build();
        httpClientBuilder
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(defaultRequestConfig);
        httpClient = httpClientBuilder.build();
    }

    public static HttpUtils getInstance() {
        return getInstance(customizer -> {
        }, false);
    }

    public static HttpUtils getInstance(InitCustomizer initializer) {
        return getInstance(initializer, false);
    }

    public static HttpUtils getInstance(boolean force) {
        return getInstance(customizer -> {
        }, force);
    }

    public static HttpUtils getInstance(InitCustomizer initializer, boolean force) {
        if (Objects.isNull(instance) || force) {
            HttpUtils http5Utils = new HttpUtils();
            http5Utils.init(initializer);
            instance = http5Utils;
        }
        return instance;
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
    public static void setMaxPerRoute(String route, int max) {
        Optional.ofNullable(connectionManager)
            .ifPresent(poolingHttpClientConnectionManager -> {
                URI uri = URI.create(route);
                HttpRoute httpRoute = new HttpRoute(new HttpHost(uri.getScheme(), uri.getHost(), uri.getPort()));
                poolingHttpClientConnectionManager.setMaxPerRoute(httpRoute, max);
            });
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper);
        OBJECT_MAPPER = objectMapper;
    }

    public static HttpClient getHttpClient() {
        return httpClient;
    }

    public void enableLoadBalancer(Function<String, URI> resolver) {
        this.loadBalancedResolver = resolver;
    }

    public HttpResult request(Consumer<Configurer> configurer) throws URISyntaxException, IOException {
        return request(null, null, configurer, httpResult -> httpResult);
    }

    public HttpResult request(String method, String url, Consumer<Configurer> configurer) throws URISyntaxException, IOException {
        return request(method, url, configurer, httpResult -> httpResult);
    }

    public <T> T request(String method, String url, Consumer<Configurer> configurer, Function<HttpResult, T> resultMapper) throws URISyntaxException, IOException {
        _assertState(Objects.nonNull(configurer), "String should not be null");
        Configurer requestConfigurer = new Configurer()
            .method(method)
            .charset(defaultCharset)
            .url(url)
            .config(RequestConfig.copy(defaultRequestConfig)
                .build());
        configurer.accept(requestConfigurer);
        _assertState(StringUtils.isNotBlank(requestConfigurer.getMethod()), "Method should not be blank");
        _assertState(StringUtils.isNotBlank(requestConfigurer.getUrl()), "URL should not be blank");
        HttpResult httpResult = request(requestConfigurer);
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

    public HttpResult request(Configurer configurer) throws IOException {
        long start = System.currentTimeMillis();
        ClassicRequestBuilder classicRequestBuilder = ClassicRequestBuilder.create(configurer.method);

        try {
            URIBuilder uriBuilder = new URIBuilder(configurer.url, configurer.charset);
            Optional.ofNullable(loadBalancedResolver)
                .map(resolver -> resolver.apply(uriBuilder.getHost()))
                .ifPresent(uri -> {
                    uriBuilder.setHost(uri.getHost());
                    uriBuilder.setPort(uri.getPort());
                });
            URI uri = uriBuilder.build();
            classicRequestBuilder.setUri(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URISyntaxException", e);
        }
        configurer.params.forEach(classicRequestBuilder::addParameter);

        if (MapUtils.isNotEmpty(configurer.headers)) {
            configurer.headers.forEach((name, values) -> values.forEach(value -> classicRequestBuilder.addHeader(name, value)));
        }

        if (StringUtils.equalsAnyIgnoreCase(classicRequestBuilder.getMethod(), HttpPost.METHOD_NAME, HttpPut.METHOD_NAME) && Objects.nonNull(configurer.httpEntity)) {
            classicRequestBuilder.setEntity(configurer.httpEntity);
        }
        ClassicHttpRequest request = classicRequestBuilder.build();
        HttpClientContext context = configurer.getContext();
        if (Objects.nonNull(configurer.config)) {
            context.setRequestConfig(configurer.config);
        }

        try {
            return httpClient.execute(request, context, response -> {
                HttpResult result = new HttpResult();
                Optional.ofNullable(context)
                    .map(HttpClientContext::getCookieStore)
                    .ifPresent(result::setCookieStore);
                Optional.of(response)
                    .ifPresent(_resp -> {
                        result.setStatusCode(_resp.getCode());
                        result.setVersion(String.valueOf(_resp.getVersion()));
                        result.setReasonPhrase(_resp.getReasonPhrase());
                        result.setHeaders(_resp.getHeaders());
                    });

                if (Objects.nonNull(response.getEntity())) {
                    HttpEntity entity = response.getEntity();
                    result.setEntity(entity);
                    Optional.ofNullable(configurer.inputConfig)
                        .ifPresent(result::setInputConfig);
                    result.read();
                }
                if (log.isDebugEnabled()) {
                    log.debug("response: cost={}, {}", Duration.ofMillis(System.currentTimeMillis() - start), response.getCode());
                }
                return result;
            });


        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("exception: cost={}, {}", Duration.ofMillis(System.currentTimeMillis() - start), e.getMessage());
            }
            throw e;
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

        public Initializer customizeSocket(Consumer<SocketConfig.Builder> customizer) {
            Optional.ofNullable(socketConfigBuilder).ifPresent(customizer);
            return this;
        }

        public Initializer customizeConnection(Consumer<ConnectionConfig.Builder> customizer) {
            Optional.ofNullable(connectionConfigBuilder).ifPresent(customizer);
            return this;
        }

        public Initializer customizeClient(Consumer<HttpClientBuilder> customizer) {
            Optional.ofNullable(httpClientBuilder).ifPresent(customizer);
            return this;
        }

        public Initializer customizeMapper(Consumer<ObjectMapper> customizer) {
            Optional.ofNullable(objectMapper).ifPresent(customizer);
            return this;
        }

    }

    @FunctionalInterface
    public interface InitCustomizer {
        void customize(Initializer customizer);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final static class Configurer {

        @Getter
        private String method;

        @Getter
        private Charset charset;

        @Getter
        private String url;

        @Getter
        private final Map<String, List<String>> headers = new HashMap<>();

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
            if (Objects.isNull(httpEntity)) {
                Body bodyModel = new Body();
                configurer.accept(bodyModel);
                AbstractBody<?> body = bodyModel.getBody();
                if (Objects.nonNull(body)) {
                    httpEntity = body.toEntity(this.charset);
                    if (StringUtils.isNotBlank(body.contentType())) {
                        header(HttpHeaders.CONTENT_TYPE, body.contentType());
                    }
                }
            }
            return this;
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

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Body {

            private AbstractBody<?> body;

            protected AbstractBody<?> getBody() {
                return body;
            }

            public Body raw(Consumer<Raw> configurer) {
                return type(Raw::new, configurer);
            }

            public Body formData(Consumer<FormData> configurer) {
                return type(FormData::new, configurer);
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
            public I contentType(ContentType contentType) {
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

            public static final ContentType TEXT_PLAIN = ContentType.TEXT_PLAIN;
            public static final ContentType APPLICATION_JSON = ContentType.APPLICATION_JSON;
            public static final ContentType TEXT_HTML = ContentType.TEXT_HTML;
            public static final ContentType APPLICATION_XML = ContentType.TEXT_XML;

            private String raw;

            public Raw() {
                super.contentType = TEXT_PLAIN;
            }

            @Override
            protected HttpEntity toEntity(Charset charset) {
                if (Objects.nonNull(contentType)) {
                    contentType = contentType.withCharset(charset);
                }
                return new StringEntity(raw, charset);
            }

            public Raw text(String text) {
                if (Objects.isNull(raw)) {
                    this.raw = text;
                    this.contentType = TEXT_PLAIN;
                }
                return this;
            }

            public Raw json(String text) {
                if (Objects.isNull(raw)) {
                    this.raw = text;
                    this.contentType = APPLICATION_JSON;
                }
                return this;
            }

            public Raw json(Object obj) {
                if (Objects.isNull(raw) && Objects.nonNull(obj)) {
                    try {
                        this.raw = OBJECT_MAPPER.writeValueAsString(obj);
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException(e);
                    }
                    this.contentType = APPLICATION_JSON;
                }
                return this;
            }

            public Raw html(String text) {
                if (Objects.isNull(raw)) {
                    this.raw = text;
                    this.contentType = TEXT_HTML;
                }
                return this;
            }

            public Raw xml(String text) {
                if (Objects.isNull(raw)) {
                    this.raw = text;
                    this.contentType = APPLICATION_XML;
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

            public FormData() {
                super.contentType = null;
            }

            @Override
            protected HttpEntity toEntity(Charset charset) {
                return builder.setCharset(charset).setContentType(ContentType.MULTIPART_FORM_DATA).build();
            }

            public FormData text(String name, String value) {
                builder.addTextBody(name, value, MULTIPART_FORM_DATA);
                return this;
            }

            public FormData file(String name, File file) {
                builder.addBinaryBody(name, file, MULTIPART_FORM_DATA, name);
                return this;
            }

            public FormData bytes(String name, byte[] bytes) {
                builder.addBinaryBody(name, bytes, MULTIPART_FORM_DATA, name);
                return this;
            }

            public FormData inputStream(String name, InputStream ips) {
                builder.addBinaryBody(name, ips, MULTIPART_FORM_DATA, name);
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
                return new UrlEncodedFormEntity(nameValuePairs, charset);
            }

            public FormUrlEncoded text(String name, String value) {
                BasicNameValuePair basicNameValuePair = new BasicNameValuePair(name, value);
                nameValuePairs.add(basicNameValuePair);
                return this;
            }

        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HttpResult {

        @Getter
        private int statusCode;
        private String version;
        private String reasonPhrase;
        @Getter
        private HttpEntity entity;
        private final Map<String, List<String>> headers = new HashMap<>();
        @Getter
        private Charset charset = StandardCharsets.UTF_8;
        private byte[] content;
        private CookieStore cookieStore;

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

        public String getHeader(String name) {
            List<String> list = headers.get(name);
            if (CollectionUtils.isNotEmpty(list)) {
                return list.get(0);
            }
            return null;
        }

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
            return cookieStore.getCookies().stream().filter(cookie -> StringUtils.equals(cookie.getName(), name)).toList();
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
                    return OBJECT_MAPPER.readValue(bytes, type);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

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

}
