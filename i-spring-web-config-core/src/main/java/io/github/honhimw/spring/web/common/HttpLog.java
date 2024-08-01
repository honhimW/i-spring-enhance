package io.github.honhimw.spring.web.common;

import io.github.honhimw.spring.IDataSize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Http logging entity
 * @author hon_him
 * @since 2023-05-06
 */

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class HttpLog implements Serializable {

    public static final String LF = System.lineSeparator();

    private String _method;

    private URI _uri;

    private int _statusCode = HttpStatus.OK.value();

    private List<Map.Entry<String, String>> _requestHeaders;

    private List<Map.Entry<String, String>> _responseHeaders;

    private final ByteArrayOutputStream _rawRequestBody = new ByteArrayOutputStream();

    private final ByteArrayOutputStream _rawResponseBody = new ByteArrayOutputStream();

    private Long _serverCost;

    public String requestLine() {
        return this._method + " " + this._uri;
    }

    public String baseInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(_statusCode).append(']').append(" ");
        sb.append(requestLine());
        sb.append(", cost: ").append(Duration.ofMillis(_serverCost));
        sb.append(", reqSize: <").append(IDataSize.of(this._rawRequestBody.size())).append(">");
        sb.append(", respSize: <").append(IDataSize.of(this. _rawResponseBody.size())).append(">");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(_statusCode).append(']').append(" ");
        sb.append(requestLine());
        sb.append(", cost: ").append(Duration.ofMillis(_serverCost));
        if (this._rawRequestBody.size() > 0) {
            sb.append(", rawReq: ");
            String string = this._rawRequestBody.toString(StandardCharsets.UTF_8);
            string = StringUtils.abbreviateMiddle(string, "...", 2048);
            string = StringUtils.remove(string, "\r\n");
            string = StringUtils.remove(string, '\n');
            string = StringUtils.remove(string, '\r');
            sb.append(string);
        }
        if (this._rawResponseBody.size() > 0) {
            sb.append(", rawResp: ");
            String string = this._rawResponseBody.toString(StandardCharsets.UTF_8);
            string = StringUtils.abbreviateMiddle(string, "...", 2048);
            string = StringUtils.remove(string, "\r\n");
            string = StringUtils.remove(string, '\n');
            string = StringUtils.remove(string, '\r');
            sb.append(string);
        }
        return sb.toString();
    }

    public String fullyInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(LF).append(">>>>>>>>>>>>>>>>").append(LF);
        sb.append('[').append(_statusCode).append(']').append(" ");
        sb.append(requestLine());
        sb.append(", cost: ").append(Duration.ofMillis(_serverCost));
        sb.append(LF);
        if (Objects.nonNull(_requestHeaders) && !_requestHeaders.isEmpty()) {
            _requestHeaders.forEach(entry -> sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(LF));
            sb.append(LF);
        }
        if (this._rawRequestBody.size() > 0) {
            sb.append(this._rawRequestBody.toString(StandardCharsets.UTF_8));
            sb.append(LF);
        }
        sb.append(">>>>>>>>>>>>>>>>").append(LF).append("<<<<<<<<<<<<<<<<").append(LF);
        if (Objects.nonNull(_responseHeaders) && !_responseHeaders.isEmpty()) {
            _responseHeaders.forEach(entry -> sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(LF));
            sb.append(LF);
        }
        if (this._rawResponseBody.size() > 0) {
            sb.append(this._rawResponseBody.toString(StandardCharsets.UTF_8));
            sb.append(LF);
        }
        sb.append("<<<<<<<<<<<<<<<<");
        return sb.toString();
    }

}
