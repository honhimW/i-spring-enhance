package io.github.honhimw.spring.web.reactive;

import io.github.honhimw.spring.annotation.resolver.FileReturn;
import org.jspecify.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.HandlerResultHandler;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.AbstractMessageWriterResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

/**
 * @author hon_him
 * @since 2024-08-09
 */

public abstract class AbstractReactiveFileResultHandler extends AbstractMessageWriterResultHandler implements HandlerResultHandler {

    protected AbstractReactiveFileResultHandler(List<HttpMessageWriter<?>> messageWriters, RequestedContentTypeResolver contentTypeResolver) {
        super(messageWriters, contentTypeResolver);
    }

    @Override
    public boolean supports(@NonNull HandlerResult result) {
        return result.getReturnTypeSource().hasMethodAnnotation(FileReturn.class);
    }

    @NonNull
    @Override
    public Mono<Void> handleResult(@NonNull ServerWebExchange exchange, @NonNull HandlerResult result) {
        Object body = result.getReturnValue();
        MethodParameter bodyTypeParameter = result.getReturnTypeSource();
        ServerHttpResponse response = exchange.getResponse();
        if (body instanceof ProblemDetail detail) {
            response.setStatusCode(HttpStatusCode.valueOf(detail.getStatus()));
            if (detail.getInstance() == null) {
                URI path = URI.create(exchange.getRequest().getPath().value());
                detail.setInstance(path);
            }
        }

        FileReturn fileReturn = bodyTypeParameter.getMethodAnnotation(FileReturn.class);
        Assert.notNull(fileReturn, "return value resolver annotation should not be null.");
        String defaultFileName = fileReturn.value();
        Assert.state(StringUtils.isNotBlank(defaultFileName), "default file name should not be blank.");

        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        if (StringUtils.isBlank(contentDisposition)) {
            response.getHeaders().setContentDisposition(ContentDisposition.attachment().filename(defaultFileName).build());
        }

        return writeBody(body, bodyTypeParameter, result.getReturnTypeSource(), exchange);
    }
}
