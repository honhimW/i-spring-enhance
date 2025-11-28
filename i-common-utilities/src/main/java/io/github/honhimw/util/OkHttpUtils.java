package io.github.honhimw.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.Experimental;
import lombok.Getter;
import okhttp3.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author honhimW
 * @since 2025-09-16
 */

@Experimental
public class OkHttpUtils {

    private OkHttpUtils() {
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
    public static final Duration READ_TIMEOUT = Duration.ofMinutes(1);

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final byte[] EMPTY_DATA = new byte[0];

    private static OkHttpUtils INSTANCE;

    private OkHttpClient okHttpClient;

    @Getter
    private ObjectMapper objectMapper;

    private void init(Consumer<OkHttpClient.Builder> consumer) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(MAX_TOTAL_CONNECTIONS);
        dispatcher.setMaxRequestsPerHost(MAX_ROUTE_CONNECTIONS);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .protocols(List.of(Protocol.HTTP_1_1, Protocol.H2_PRIOR_KNOWLEDGE))
            .connectionSpecs(List.of(ConnectionSpec.CLEARTEXT, ConnectionSpec.COMPATIBLE_TLS))
            .connectTimeout(CONNECT_TIMEOUT)
            .readTimeout(READ_TIMEOUT)
            .retryOnConnectionFailure(true)
            .dispatcher(dispatcher);
        consumer.accept(builder);
        okHttpClient = builder.build();
        objectMapper = JsonUtils.mapper().copy();
    }

    public static OkHttpUtils getSharedInstance() {
        if (Objects.isNull(INSTANCE)) {
            buildSharedInstance(customizer -> {
            });
        }
        return INSTANCE;
    }

    public static void buildSharedInstance(Consumer<OkHttpClient.Builder> consumer) {
        INSTANCE = newInstance(consumer);
    }

    public static OkHttpUtils newInstance() {
        return newInstance(customizer -> {
        });
    }

    public static OkHttpUtils newInstance(Consumer<OkHttpClient.Builder> consumer) {
        OkHttpUtils okHttpUtils = new OkHttpUtils();
        okHttpUtils.init(consumer);
        return okHttpUtils;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper);
        this.objectMapper = objectMapper;
    }

    public OkHttpClient getHttpClient() {
        return okHttpClient;
    }

    public void execute() {

        Call call = okHttpClient.newCall(null);
//        Response execute = call.execute();
    }

    public final static class Configurer {
        private final OkHttpUtils self;

        private Configurer(OkHttpUtils self) {
            this.self = self;
        }

        private String method;

        private Charset charset;

        private String url;

        private final Map<CharSequence, List<CharSequence>> headers = new LinkedHashMap<>();

        private final List<Map.Entry<String, String>> params = new ArrayList<>();

        public Configurer method(String method) {
            this.method = method;
            return this;
        }

        public Configurer url(String url) {
            this.url = url;
            return this;
        }

        public Configurer charset(Charset charset) {
            this.charset = charset;
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
        public List<Map.Entry<String, String>> params() {
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

    }

}
