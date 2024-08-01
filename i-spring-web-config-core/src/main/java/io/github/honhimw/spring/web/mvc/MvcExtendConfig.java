package io.github.honhimw.spring.web.mvc;

import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * @author hon_him
 * @since 2023-05-09
 */

public class MvcExtendConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(@Nonnull List<HttpMessageConverter<?>> converters) {
        converters.removeIf(AbstractJackson2HttpMessageConverter.class::isInstance);
        converters.add(new FetcherJacksonConverter());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods(HttpMethod.POST.name(), HttpMethod.GET.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name())
            .allowedHeaders("*")
            .allowCredentials(true);
    }

}
