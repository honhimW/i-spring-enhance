package io.github.honhimw.example.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.honhimw.spring.web.common.resolver.reactive.CsvJackson2Encoder;
import io.github.honhimw.spring.web.common.resolver.reactive.CsvJacksonNodeReactiveCustomizer;
import io.github.honhimw.spring.web.common.resolver.reactive.CsvReactiveParamResolver;
import io.github.honhimw.spring.web.reactive.ReactiveLoggingRebinderEndpointFilter;
import io.github.honhimw.util.JsonUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.AbstractJackson2Encoder;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

/**
 * @author hon_him
 * @since 2023-05-22
 */

@Configuration
//@EnableWebFlux
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

//    @Bean
//    WebFluxConfigurer otherWebFluxConfigurer() {
//        return new WebFluxConfigurer() {
//            @Override
//            public void configureArgumentResolvers(@NonNull ArgumentResolverConfigurer configurer) {
//                configurer.addCustomResolver(new CsvReactiveParamResolver(JsonUtils.mapper()));
//            }
//
//            @Override
//            public void configureHttpMessageCodecs(@NonNull ServerCodecConfigurer configurer) {
//                configurer.customCodecs().registerWithDefaultConfig(new CsvJackson2Encoder(JsonUtils.mapper()));
//
//                configurer.customCodecs().registerWithDefaultConfig(new AbstractJackson2Encoder(new XmlMapper(), MediaType.APPLICATION_XML) {
//
//                });
//            }
//        };
//    }

//    @Bean
    WebFluxConfigurer otherWebFluxConfigurer() {
        return new WebFluxConfigurer() {
            @Override
            public void configureHttpMessageCodecs(@NonNull ServerCodecConfigurer configurer) {
                configurer.customCodecs().registerWithDefaultConfig(new AbstractJackson2Encoder(new XmlMapper(), MediaType.APPLICATION_XML) {

                });
            }
        };
    }

}