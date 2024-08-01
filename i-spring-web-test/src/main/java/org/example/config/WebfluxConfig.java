package org.example.config;

import io.github.honhimw.spring.util.JsonUtils;
import io.github.honhimw.spring.web.common.resolver.reactive.CsvJacksonNodeReactiveCustomizer;
import io.github.honhimw.spring.web.reactive.ReactiveLoggingRebinderEndpointFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * @author hon_him
 * @since 2023-05-22
 */

@Configuration
@EnableWebFlux
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class WebfluxConfig {

    @Bean
    ReactiveLoggingRebinderEndpointFilter reactiveLoggingRebinderEndpointFilter() {
        return new ReactiveLoggingRebinderEndpointFilter();
    }

    @Bean
    CsvJacksonNodeReactiveCustomizer csvJacksonNodeReactiveCustomizer() {
        return new CsvJacksonNodeReactiveCustomizer(JsonUtils.getObjectMapper());
    }

}
