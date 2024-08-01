package io.github.honhimw.spring.web.mvc;

import com.fasterxml.jackson.core.JsonGenerator;
import io.github.honhimw.spring.util.JacksonFilterUtils;
import io.github.honhimw.spring.web.common.WebConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2023-05-17
 */

public class FetcherJacksonConverter extends MvcJackson2HttpMessageConverter {

    @Nonnull
    @Override
    protected JsonGenerator decorateGenerator(@Nonnull JsonGenerator generator) {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
            .filter(ServletRequestAttributes.class::isInstance)
            .map(ServletRequestAttributes.class::cast)
            .map(ServletRequestAttributes::getRequest)
            .flatMap(request -> include(generator, request).or(() -> exclude(generator, request)))
            .orElse(generator);
    }

    protected Optional<JsonGenerator> include(JsonGenerator generator, HttpServletRequest request) {
        return Optional.ofNullable(request)
            .map(r -> r.getHeader(WebConstants.FETCH_ONLY_INCLUDE))
            .filter(StringUtils::isNotBlank)
            .map(s -> StringUtils.split(s, ';'))
            .map(paths -> JacksonFilterUtils.includeFilter(generator, paths));
    }

    protected Optional<JsonGenerator> exclude(JsonGenerator generator, HttpServletRequest request) {
        return Optional.ofNullable(request)
            .map(r -> r.getHeader(WebConstants.FETCH_NON_EXCLUDE))
            .filter(StringUtils::isNotBlank)
            .map(s -> StringUtils.split(s, ';'))
            .map(paths -> JacksonFilterUtils.excludeFilter(generator, paths));
    }

}
