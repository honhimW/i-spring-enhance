package io.github.honhimw.spring.web;

import io.github.honhimw.spring.web.common.ExceptionWrappers;
import io.github.honhimw.spring.web.common.resolver.FormDataParamResolver;
import io.github.honhimw.spring.web.common.resolver.JacksonNodeCustomizer;
import io.github.honhimw.spring.web.common.resolver.TextParamResolver;
import io.github.honhimw.spring.web.common.resolver.reactive.JacksonNodeReactiveCustomizer;
import io.github.honhimw.spring.web.common.resolver.reactive.TextReactiveParamResolver;
import io.github.honhimw.spring.web.reactive.WebFluxJackson2Decoder;
import io.github.honhimw.spring.web.reactive.WebFluxJackson2Encoder;
import jakarta.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.http.codec.json.AbstractJackson2Encoder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

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

        @Bean(name = "iSpringResolverMvcConfigurer")
        @ConditionalOnMissingBean(name = "iSpringResolverMvcConfigurer")
        WebMvcConfigurer iSpringResolverMvcConfigurer(@Autowired(required = false) List<JacksonNodeCustomizer> customizers) {
            return new WebMvcConfigurer() {
                @Override
                public void addArgumentResolvers(@Nonnull List<HandlerMethodArgumentResolver> resolvers) {
                    TextParamResolver textParamResolver = new TextParamResolver();
                    FormDataParamResolver formDataParamResolver = new FormDataParamResolver();
                    if (CollectionUtils.isNotEmpty(customizers)) {
                        customizers.forEach(textParamResolver::addJacksonNodeCustomizer);
                        customizers.forEach(formDataParamResolver::addJacksonNodeCustomizer);
                    }
                    resolvers.add(textParamResolver);
                    resolvers.add(formDataParamResolver);
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
        AbstractJackson2Decoder webFluxJackson2Decoder() {
            return new WebFluxJackson2Decoder();
        }

        @Bean(name = "webFluxJackson2Encoder")
        @ConditionalOnMissingBean(name = "webFluxJackson2Encoder")
        AbstractJackson2Encoder webFluxJackson2Encoder() {
            return new WebFluxJackson2Encoder();
        }

        @Bean(name = "iSpringResolverReactiveConfigurer")
        @ConditionalOnMissingBean(name = "iSpringResolverReactiveConfigurer")
        WebFluxConfigurer iSpringResolverReactiveConfigurer(@Autowired(required = false) List<JacksonNodeReactiveCustomizer> customizers) {
            return new WebFluxConfigurer() {
                @Override
                public void configureArgumentResolvers(@Nonnull ArgumentResolverConfigurer configurer) {
                    TextReactiveParamResolver textReactiveParamResolver = new TextReactiveParamResolver();
                    if (CollectionUtils.isNotEmpty(customizers)) {
                        customizers.forEach(textReactiveParamResolver::addJacksonNodeCustomizer);
                    }
                    configurer.addCustomResolver(textReactiveParamResolver);
                }

                @Override
                public void configureHttpMessageCodecs(@Nonnull ServerCodecConfigurer configurer) {
                    ServerCodecConfigurer.ServerDefaultCodecs serverDefaultCodecs = configurer.defaultCodecs();
                    serverDefaultCodecs.jackson2JsonDecoder(webFluxJackson2Decoder());
                    serverDefaultCodecs.jackson2JsonEncoder(webFluxJackson2Encoder());
                }
            };
        }
    }

}
