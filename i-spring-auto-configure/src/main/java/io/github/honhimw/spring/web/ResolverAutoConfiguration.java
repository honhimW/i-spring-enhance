package io.github.honhimw.spring.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.spring.web.common.ExceptionWrappers;
import io.github.honhimw.spring.web.common.resolver.FormDataParamResolver;
import io.github.honhimw.spring.web.common.resolver.JacksonNodeCustomizer;
import io.github.honhimw.spring.web.common.resolver.TextParamResolver;
import io.github.honhimw.spring.web.common.resolver.reactive.JacksonNodeReactiveCustomizer;
import io.github.honhimw.spring.web.common.resolver.reactive.TextReactiveParamResolver;
import io.github.honhimw.spring.web.reactive.WebFluxJackson2Decoder;
import io.github.honhimw.spring.web.reactive.WebFluxJackson2Encoder;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.http.codec.json.AbstractJackson2Encoder;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2023-03-13
 */

@ConditionalOnClass(ExceptionWrappers.class)
@Configuration(proxyBeanMethods = false)
public class ResolverAutoConfiguration {

    /**
     * WebMVC support
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = Type.SERVLET)
    static class Resolver4SpringWebMvc {

        @Bean(name = "extendResolverMvcConfigurer")
        @ConditionalOnMissingBean(name = "extendResolverMvcConfigurer")
        WebMvcConfigurer extendResolverMvcConfigurer(
            ObjectProvider<JacksonNodeCustomizer> customizers,
            ObjectProvider<AbstractJackson2HttpMessageConverter> jackson2HttpMessageConverterProvider
        ) {
            return new WebMvcConfigurer() {
                @Override
                public void addArgumentResolvers(@Nonnull List<HandlerMethodArgumentResolver> resolvers) {
                    AbstractJackson2HttpMessageConverter converter = jackson2HttpMessageConverterProvider.getIfAvailable();
                    if (Objects.nonNull(converter)) {
                        TextParamResolver textParamResolver = new TextParamResolver(converter);
                        FormDataParamResolver formDataParamResolver = new FormDataParamResolver(converter);
                        customizers.orderedStream().forEach(textParamResolver::addJacksonNodeCustomizer);
                        customizers.orderedStream().forEach(formDataParamResolver::addJacksonNodeCustomizer);
                        resolvers.add(textParamResolver);
                        resolvers.add(formDataParamResolver);
                    }
                }
            };
        }
    }

    /**
     * WebFlux support
     */
    @Configuration
    @ConditionalOnWebApplication(type = Type.REACTIVE)
    static class Resolver4SpringWebFlux {

        @Bean(name = "webFluxJackson2Decoder")
        @ConditionalOnMissingBean(name = "webFluxJackson2Decoder")
        AbstractJackson2Decoder webFluxJackson2Decoder(ObjectMapper objectMapper) {
            return new WebFluxJackson2Decoder(objectMapper, MediaType.APPLICATION_JSON);
        }

        @Bean(name = "webFluxJackson2Encoder")
        @ConditionalOnMissingBean(name = "webFluxJackson2Encoder")
        AbstractJackson2Encoder webFluxJackson2Encoder(ObjectMapper objectMapper) {
            return new WebFluxJackson2Encoder(objectMapper, MediaType.APPLICATION_JSON);
        }

        @Bean(name = "extendResolverReactiveConfigurer")
        @ConditionalOnMissingBean(name = "extendResolverReactiveConfigurer")
        WebFluxConfigurer extendResolverReactiveConfigurer(
            ObjectProvider<JacksonNodeReactiveCustomizer> customizers,
            AbstractJackson2Decoder webFluxJackson2Decoder,
            AbstractJackson2Encoder webFluxJackson2Encoder
        ) {
            return new WebFluxConfigurer() {

                @Override
                public void configureArgumentResolvers(@Nonnull ArgumentResolverConfigurer configurer) {
                    TextReactiveParamResolver textReactiveParamResolver = new TextReactiveParamResolver(webFluxJackson2Decoder);
                    customizers.orderedStream().forEach(textReactiveParamResolver::addJacksonNodeCustomizer);
                    configurer.addCustomResolver(textReactiveParamResolver);
                }



                @Override
                public void configureHttpMessageCodecs(@Nonnull ServerCodecConfigurer configurer) {
                    ServerCodecConfigurer.ServerDefaultCodecs serverDefaultCodecs = configurer.defaultCodecs();
                    serverDefaultCodecs.jackson2JsonDecoder(webFluxJackson2Decoder);
                    serverDefaultCodecs.jackson2JsonEncoder(webFluxJackson2Encoder);

                }
            };
        }
    }

}
