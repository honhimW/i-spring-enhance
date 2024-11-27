package org.example.config;

import io.github.honhimw.spring.web.common.resolver.reactive.CsvJackson2Encoder;
import io.github.honhimw.spring.web.common.resolver.reactive.CsvJacksonNodeReactiveCustomizer;
import io.github.honhimw.spring.web.common.resolver.reactive.CsvReactiveParamResolver;
import io.github.honhimw.spring.web.reactive.ReactiveLoggingRebinderEndpointFilter;
import io.github.honhimw.util.JsonUtils;
import jakarta.annotation.Nonnull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

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
        return new CsvJacksonNodeReactiveCustomizer(JsonUtils.mapper());
    }

    @Bean
    WebFluxConfigurer otherWebFluxConfigurer() {
        return new WebFluxConfigurer() {
            @Override
            public void configureArgumentResolvers(@Nonnull ArgumentResolverConfigurer configurer) {
                configurer.addCustomResolver(new CsvReactiveParamResolver(JsonUtils.mapper()));
            }

            @Override
            public void configureHttpMessageCodecs(@Nonnull ServerCodecConfigurer configurer) {
                configurer.customCodecs().registerWithDefaultConfig(new CsvJackson2Encoder(JsonUtils.mapper()));
            }
        };
    }

}