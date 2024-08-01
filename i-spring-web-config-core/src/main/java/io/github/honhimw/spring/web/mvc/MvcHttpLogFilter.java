package io.github.honhimw.spring.web.mvc;

import io.github.honhimw.spring.web.common.HttpLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * 日志输出过滤器,
 *
 * @author hon_him
 * @since 2023-05-19
 */

@Slf4j
public class MvcHttpLogFilter extends OncePerRequestFilter implements Ordered {

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        // only work on TRACE/DEBUG/INFO level
        if (!log.isInfoEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // wrap caching request/response
        ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        // do filter
        long pre = System.currentTimeMillis();
        filterChain.doFilter(cachingRequest, cachingResponse);
        long post = System.currentTimeMillis();
        long cost = post - pre;

        // construct http-log entity
        HttpLog httpLog = new HttpLog();
        StringBuilder sb = new StringBuilder();
        httpLog.set_statusCode(cachingResponse.getStatus());
        sb.append(cachingRequest.getRequestURL());
        if (StringUtils.isNotBlank(cachingRequest.getQueryString())) {
            sb.append("?");
            sb.append(cachingRequest.getQueryString());
        }
        String string = sb.toString();
        URI uri = URI.create(string);
        httpLog.set_uri(uri);
        httpLog.set_method(cachingRequest.getMethod());
        httpLog.set_serverCost(cost);

        // only raw-type request content will be recorded
        if (isRawType(cachingRequest.getContentType())) {
            ByteArrayOutputStream rawRequestBody = httpLog.get_rawRequestBody();
            byte[] bytes = cachingRequest.getContentAsByteArray();
            rawRequestBody.writeBytes(bytes);
        }

        // ContentCachingResponseWrapper contents data are cached in memory.
        // The copyBodyToResponse() needs to be called to write data to
        // the output channel of the very original response.
        // After the replication is complete, the cached data will be reset.
        byte[] bytes = cachingResponse.getContentAsByteArray();
        cachingResponse.copyBodyToResponse();

        // only raw-type response content will be recorded
        if (isRawType(cachingResponse.getContentType())) {
            ByteArrayOutputStream rawResponseBody = httpLog.get_rawResponseBody();
            rawResponseBody.write(bytes);
        }

        if (log.isTraceEnabled()) {
            {
                Enumeration<String> headerNames = cachingRequest.getHeaderNames();
                List<Map.Entry<String, String>> requestHeader = new ArrayList<>();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    Enumeration<String> headers = cachingRequest.getHeaders(headerName);
                    while (headers.hasMoreElements()) {
                        String headerValue = headers.nextElement();
                        requestHeader.add(Map.entry(headerName, headerValue));
                    }
                }
                httpLog.set_requestHeaders(requestHeader);
            }
            {
                Collection<String> headerNames = cachingResponse.getHeaderNames();
                List<Map.Entry<String, String>> responseHeader = new ArrayList<>();
                headerNames.forEach(headerName -> {
                    Collection<String> headers = cachingResponse.getHeaders(headerName);
                    headers.forEach(headerValue -> responseHeader.add(Map.entry(headerName, headerValue)));
                });
                httpLog.set_responseHeaders(responseHeader);
            }
            log.trace(httpLog.fullyInfo());
        } else if (log.isDebugEnabled()) {
            log.debug(httpLog.toString());
        } else if (log.isInfoEnabled()) {
            log.info(httpLog.baseInfo());
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isRawType(String mimeType) {
        if (StringUtils.isBlank(mimeType)) {
            return false;
        }
        return isRawType(MimeType.valueOf(mimeType));
    }

    private boolean isRawType(MimeType mimeType) {
        return MediaType.APPLICATION_JSON.isCompatibleWith(mimeType)
            || MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mimeType)
            || MediaType.APPLICATION_XML.isCompatibleWith(mimeType)
            || MediaType.TEXT_PLAIN.isCompatibleWith(mimeType)
            || MediaType.TEXT_XML.isCompatibleWith(mimeType)
            ;
    }

}
