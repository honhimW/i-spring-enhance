package io.github.honhimw.spring.web.mvc;

import io.github.honhimw.spring.web.common.HttpLog;
import io.github.honhimw.spring.web.util.MimeTypeSupports;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

/**
 * MVC http log filter
 *
 * @author hon_him
 * @since 2024-11-20
 */

public class MvcHttpLogFilter extends OncePerRequestFilter implements Ordered {

    public static final int DEFAULT_FILTER_ORDERED = -1000;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        if (!HttpLog.log.isInfoEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // construct http-log entity
        HttpLog httpLog = new HttpLog();
        StringBuilder sb = new StringBuilder();

        String encoding = request.getCharacterEncoding();

        if (StringUtils.isNotBlank(encoding)) {
            Charset charset = Charset.forName(encoding);
            httpLog.setCharset(charset);
        }

        sb.append(request.getRequestURL());
        if (StringUtils.isNotBlank(request.getQueryString())) {
            sb.append("?");
            sb.append(request.getQueryString());
        }
        String string = sb.toString();
        URI uri = URI.create(string);
        httpLog.setUri(uri);
        httpLog.setMethod(request.getMethod());
        HttpLog.LogHolder logHolder = new HttpLog.LogHolder(httpLog);
        request.setAttribute(HttpLog.LogHolder.class.getName(), logHolder);

        // wrap caching request/response
        ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        // do filter
        doFilter(request, response, filterChain, logHolder);
        httpLog = logHolder.get();

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

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, HttpLog.LogHolder logHolder) throws ServletException, IOException {
        long pre = System.currentTimeMillis();
        filterChain.doFilter(request, response);
        long post = System.currentTimeMillis();
        long elapsed = post - pre;
        HttpLog httpLog = logHolder.get();
        httpLog.setStatus(response.getStatus());
        httpLog.setElapsed(elapsed);
    }

    @Override
    public int getOrder() {
        return DEFAULT_FILTER_ORDERED;
    }

}
