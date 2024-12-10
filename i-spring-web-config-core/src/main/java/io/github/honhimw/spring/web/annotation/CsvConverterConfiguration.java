package io.github.honhimw.spring.web.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import io.github.honhimw.spring.web.common.resolver.CsvHttpMessageConverter;
import io.github.honhimw.spring.web.common.resolver.CsvPartMessageConverterProcessor;
import io.github.honhimw.spring.web.common.resolver.reactive.CsvJackson2Encoder;
import io.github.honhimw.spring.web.common.resolver.reactive.CsvReactiveFileResultHandler;
import io.github.honhimw.spring.web.common.resolver.reactive.CsvReactiveParamResolver;
import jakarta.annotation.Nonnull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author hon_him
 * @since 2024-08-09
 */

@Import({
    CsvConverterConfiguration.MvcCsvConverterConfiguration.class,
    CsvConverterConfiguration.ReactiveCsvConverterConfiguration.class
})
@ConditionalOnClass(CsvMapper.class)
class CsvConverterConfiguration {

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class MvcCsvConverterConfiguration {

        @Bean("mvcCsvConverterConfiguration")
        @ConditionalOnMissingBean(name = "mvcCsvConverterConfiguration")
        WebMvcConfigurer mvcCsvConverterConfiguration(ObjectMapper objectMapper) {
            CsvHttpMessageConverter converter = new CsvHttpMessageConverter(objectMapper);
            CsvPartMessageConverterProcessor csvPartMessageConverterProcessor = new CsvPartMessageConverterProcessor(List.of(converter));
            return new WebMvcConfigurer() {
                @Override
                public void addArgumentResolvers(@Nonnull List<HandlerMethodArgumentResolver> resolvers) {
                    resolvers.add(csvPartMessageConverterProcessor);
                }

                @Override
                public void addReturnValueHandlers(@Nonnull List<HandlerMethodReturnValueHandler> handlers) {
                    handlers.add(csvPartMessageConverterProcessor);
                }

                @Override
                public void extendMessageConverters(@Nonnull List<HttpMessageConverter<?>> converters) {
                    converters.add(converter);
                }
            };
        }

    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveCsvConverterConfiguration {

        @Bean("csvReactiveFileResultHandler")
        @ConditionalOnMissingBean(name = "csvReactiveFileResultHandler")
        CsvReactiveFileResultHandler csvReactiveFileResultHandler(ObjectMapper objectMapper) {
            HttpMessageWriter<?> messageWriter = new EncoderHttpMessageWriter<>(new CsvJackson2Encoder(objectMapper));
            return new CsvReactiveFileResultHandler(List.of(messageWriter));
        }

        @Bean("webFluxCsvConverterConfiguration")
        @ConditionalOnMissingBean(name = "webFluxCsvConverterConfiguration")
        WebFluxConfigurer webFluxCsvConverterConfiguration(ObjectMapper objectMapper) {
            return new WebFluxConfigurer() {
                @Override
                public void configureArgumentResolvers(@Nonnull ArgumentResolverConfigurer configurer) {
                    configurer.addCustomResolver(new CsvReactiveParamResolver(objectMapper));
                }

                @Override
                public void configureHttpMessageCodecs(@Nonnull ServerCodecConfigurer configurer) {
                    configurer.customCodecs().registerWithDefaultConfig(new CsvJackson2Encoder(objectMapper));
                }
            };
        }

    }
}
