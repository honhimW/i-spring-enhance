package io.github.honhimw.spring.web.common;

import io.github.honhimw.util.IDataSize;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Http logging entity
 *
 * @author hon_him
 * @since 2024-11-20
 */

@Getter
@Setter
public class HttpLog implements Serializable {

    protected static final Logger log = LoggerFactory.getLogger("HTTP_LOG");
    protected static final Logger headerLog = LoggerFactory.getLogger("HTTP_LOG.HEADER");
    protected static final Logger bodyLog = LoggerFactory.getLogger("HTTP_LOG.BODY");

    private String method;

    private URI uri;

    private int status = HttpStatus.OK.value();

    private List<Map.Entry<String, String>> requestHeaders;

    private List<Map.Entry<String, String>> responseHeaders;

    private ByteArrayOutputStream rawRequestBody = new ByteArrayOutputStream();

    private ByteArrayOutputStream rawResponseBody = new ByteArrayOutputStream();

    private long elapsed = 0L;

    private boolean printRequestBody = true;

    private boolean printResponseBody = true;

    @Override
    public String toString() {
        return "[%s] %s".formatted(status, requestLine());
    }

    public String requestLine() {
        return this.method + " " + this.uri;
    }

    public void log() {
        if (log.isTraceEnabled()) {
            trace();
        } else if (log.isDebugEnabled()) {
            debug();
        } else if (log.isInfoEnabled()) {
            info();
        } else if (getStatus() >= 400) {
            log.error(toString());
        }
    }

    public void info() {
        String sb = "[" + getStatus() + "] " + getMethod() + " " + getUri() +
                    " [ET: " + Duration.ofMillis(getElapsed()) + "]" +
                    " [RQ: <" + IDataSize.of(getRawRequestBody().size()) + ">]" +
                    " [RP: <" + IDataSize.of(getRawResponseBody().size()) + ">]";
        log.info(sb);
    }

    public void debug() {
        StringBuilder sb = new StringBuilder()
            .append("[").append(getStatus()).append("] ").append(getMethod()).append(" ").append(getUri())
            .append(" [ET: ").append(Duration.ofMillis(getElapsed())).append("]");
        if (getRawRequestBody().size() > 0) {
            sb.append(" [RQ: ");
            String string = requestBodyToString();
            string = StringUtils.abbreviateMiddle(string, "...", 2048);
            string = StringUtils.remove(string, "\r\n");
            string = StringUtils.remove(string, '\n');
            string = StringUtils.remove(string, '\r');
            sb.append(string).append("]");
        }
        if (getRawResponseBody().size() > 0) {
            sb.append(" [RP: ");
            String string = responseBodyToString();
            string = StringUtils.abbreviateMiddle(string, "...", 2048);
            string = StringUtils.remove(string, "\r\n");
            string = StringUtils.remove(string, '\n');
            string = StringUtils.remove(string, '\r');
            sb.append(string).append("]");
        }
        log.debug(sb.toString());
    }

    public void trace() {
        String baseInfo = "[" + getStatus() + "] " + getMethod() + " " + getUri() +
                          " [ET: " + Duration.ofMillis(getElapsed()) + "]" +
                          " [RQ: <" + IDataSize.of(getRawRequestBody().size()) + ">]" +
                          " [RP: <" + IDataSize.of(getRawResponseBody().size()) + ">]";
        log.trace(baseInfo);
        List<Map.Entry<String, String>> requestHeaders = getRequestHeaders();
        if (Objects.nonNull(requestHeaders) && !requestHeaders.isEmpty()) {
            for (Map.Entry<String, String> requestHeader : requestHeaders) {
                headerLog.trace(">>> {}: {}", requestHeader.getKey(), requestHeader.getValue());
            }
        }

        if (getRawRequestBody().size() > 0) {
            bodyLog.trace(">>> {}", requestBodyToString());
        }

        List<Map.Entry<String, String>> responseHeaders = getResponseHeaders();
        if (Objects.nonNull(responseHeaders) && !responseHeaders.isEmpty()) {
            for (Map.Entry<String, String> responseHeader : responseHeaders) {
                headerLog.trace("<<< {}: {}", responseHeader.getKey(), responseHeader.getValue());
            }
        }

        if (getRawResponseBody().size() > 0) {
            bodyLog.trace("<<< {}", responseBodyToString());
        }
    }

    protected String requestBodyToString() {
        if (printRequestBody) {
            return this.rawRequestBody.toString(StandardCharsets.UTF_8);
        }
        return "<" + IDataSize.of(this.rawRequestBody.size()) + ">";
    }

    protected String responseBodyToString() {
        if (printResponseBody) {
            return this.rawResponseBody.toString(StandardCharsets.UTF_8);
        }
        return "<" + IDataSize.of(this.rawResponseBody.size()) + ">";
    }

    public static class LogHolder {
        private final AtomicReference<HttpLog> ref;

        public LogHolder(HttpLog httpLog) {
            this.ref = new AtomicReference<>(httpLog);
        }

        public HttpLog get() {
            return this.ref.get();
        }

        public void set(HttpLog httpLog) {
            this.ref.set(httpLog);
        }
    }

    public static class Delegate extends HttpLog {
        private final HttpLog httpLog;

        public Delegate(HttpLog httpLog) {
            this.httpLog = httpLog;
        }

        @Override
        public String getMethod() {
            return httpLog.getMethod();
        }

        @Override
        public URI getUri() {
            return httpLog.getUri();
        }

        @Override
        public int getStatus() {
            return httpLog.getStatus();
        }

        @Override
        public List<Map.Entry<String, String>> getRequestHeaders() {
            return httpLog.getRequestHeaders();
        }

        @Override
        public List<Map.Entry<String, String>> getResponseHeaders() {
            return httpLog.getResponseHeaders();
        }

        @Override
        public ByteArrayOutputStream getRawRequestBody() {
            return httpLog.getRawRequestBody();
        }

        @Override
        public ByteArrayOutputStream getRawResponseBody() {
            return httpLog.getRawResponseBody();
        }

        @Override
        public long getElapsed() {
            return httpLog.getElapsed();
        }

        @Override
        public boolean isPrintRequestBody() {
            return httpLog.isPrintRequestBody();
        }

        @Override
        public boolean isPrintResponseBody() {
            return httpLog.isPrintResponseBody();
        }

        @Override
        public void setMethod(String method) {
            httpLog.setMethod(method);
        }

        @Override
        public void setUri(URI uri) {
            httpLog.setUri(uri);
        }

        @Override
        public void setStatus(int status) {
            httpLog.setStatus(status);
        }

        @Override
        public void setRequestHeaders(List<Map.Entry<String, String>> requestHeaders) {
            httpLog.setRequestHeaders(requestHeaders);
        }

        @Override
        public void setResponseHeaders(List<Map.Entry<String, String>> responseHeaders) {
            httpLog.setResponseHeaders(responseHeaders);
        }

        @Override
        public void setRawRequestBody(ByteArrayOutputStream rawRequestBody) {
            httpLog.setRawRequestBody(rawRequestBody);
        }

        @Override
        public void setRawResponseBody(ByteArrayOutputStream rawResponseBody) {
            httpLog.setRawResponseBody(rawResponseBody);
        }

        @Override
        public void setElapsed(long elapsed) {
            httpLog.setElapsed(elapsed);
        }

        @Override
        public void setPrintRequestBody(boolean printRequestBody) {
            httpLog.setPrintRequestBody(printRequestBody);
        }

        @Override
        public void setPrintResponseBody(boolean printResponseBody) {
            httpLog.setPrintResponseBody(printResponseBody);
        }

        @Override
        public String toString() {
            return httpLog.toString();
        }

        @Override
        public String requestLine() {
            return httpLog.requestLine();
        }

        @Override
        public void log() {
            httpLog.log();
        }

        @Override
        public void info() {
            httpLog.info();
        }

        @Override
        public void debug() {
            httpLog.debug();
        }

        @Override
        public void trace() {
            httpLog.trace();
        }

        @Override
        public String requestBodyToString() {
            return httpLog.requestBodyToString();
        }

        @Override
        public String responseBodyToString() {
            return httpLog.responseBodyToString();
        }
    }

}
