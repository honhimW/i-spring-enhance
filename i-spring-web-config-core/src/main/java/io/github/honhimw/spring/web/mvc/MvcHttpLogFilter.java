package io.github.honhimw.spring.web.mvc;

import io.github.honhimw.spring.web.common.HttpLog;
import io.github.honhimw.spring.web.util.MimeTypeSupports;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * Http log filter
 *
 * @author hon_him
 * @since 2024-11-20
 */

@Slf4j
public class MvcHttpLogFilter extends OncePerRequestFilter implements Ordered {

    public static final int DEFAULT_FILTER_ORDERED = -1000;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        // only work on TRACE/DEBUG/INFO
        if (!log.isInfoEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // wrap caching request/response
        ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        // construct http-log entity
        HttpLog httpLog = new HttpLog();
        StringBuilder sb = new StringBuilder();
        httpLog.setStatus(cachingResponse.getStatus());
        sb.append(cachingRequest.getRequestURL());
        if (StringUtils.isNotBlank(cachingRequest.getQueryString())) {
            sb.append("?");
            sb.append(cachingRequest.getQueryString());
        }
        String string = sb.toString();
        URI uri = URI.create(string);
        httpLog.setUri(uri);
        httpLog.setMethod(cachingRequest.getMethod());
        HttpLog.LogHolder logHolder = new HttpLog.LogHolder(httpLog);
        cachingRequest.setAttribute(HttpLog.LogHolder.class.getName(), logHolder);

        // do filter
        long pre = System.currentTimeMillis();
        filterChain.doFilter(cachingRequest, cachingResponse);
        long post = System.currentTimeMillis();
        long elapsed = post - pre;

        httpLog = logHolder.get();
        httpLog.setElapsed(elapsed);

        // only raw-type request content will be recorded
        if (MimeTypeSupports.isRawType(cachingRequest.getContentType())) {
            ByteArrayOutputStream rawRequestBody = httpLog.getRawRequestBody();
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
        if (MimeTypeSupports.isRawType(cachingResponse.getContentType())) {
            ByteArrayOutputStream rawResponseBody = httpLog.getRawResponseBody();
            rawResponseBody.write(bytes);
        }

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
            httpLog.setRequestHeaders(requestHeader);
        }

        {
            Collection<String> headerNames = cachingResponse.getHeaderNames();
            List<Map.Entry<String, String>> responseHeader = new ArrayList<>();
            headerNames.forEach(headerName -> {
                Collection<String> headers = cachingResponse.getHeaders(headerName);
                headers.forEach(headerValue -> responseHeader.add(Map.entry(headerName, headerValue)));
            });
            httpLog.setResponseHeaders(responseHeader);
        }

        httpLog.log();
    }

    @Override
    public int getOrder() {
        return DEFAULT_FILTER_ORDERED;
    }

}
