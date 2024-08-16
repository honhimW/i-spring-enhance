package io.github.honhimw.spring.web.reactive;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.spring.util.JacksonFilterUtils;
import io.github.honhimw.spring.web.common.WebConstants;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMessage;
import org.springframework.http.codec.json.AbstractJackson2Encoder;
import org.springframework.util.MimeType;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

/**
 * @author hon_him
 * @since 2023-05-17
 */

public class FetcherJackson2Encoder extends AbstractJackson2Encoder {

    public FetcherJackson2Encoder(ObjectMapper mapper, MimeType... mimeTypes) {
        super(mapper, mimeTypes);
    }

    @Nonnull
    protected JsonGenerator decorateGenerator(@Nonnull JsonGenerator generator) {
        return Optional.ofNullable(ExchangeHolder.getExchange())
            .map(ServerWebExchange::getRequest)
            .map(HttpMessage::getHeaders)
            .flatMap(httpHeaders -> include(generator, httpHeaders).or(() -> exclude(generator, httpHeaders)))
            .orElse(generator);
    }

    protected Optional<JsonGenerator> include(JsonGenerator generator, HttpHeaders headers) {
        return Optional.ofNullable(headers)
            .map(r -> r.getFirst(WebConstants.FETCH_ONLY_INCLUDE))
            .filter(StringUtils::isNotBlank)
            .map(s -> StringUtils.split(s, ';'))
            .map(paths -> JacksonFilterUtils.includeFilter(generator, paths));
    }

    protected Optional<JsonGenerator> exclude(JsonGenerator generator, HttpHeaders headers) {
        return Optional.ofNullable(headers)
            .map(r -> r.getFirst(WebConstants.FETCH_NON_EXCLUDE))
            .filter(StringUtils::isNotBlank)
            .map(s -> StringUtils.split(s, ';'))
            .map(paths -> JacksonFilterUtils.excludeFilter(generator, paths));
    }

}
