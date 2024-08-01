package io.github.honhimw.spring.web;

import io.github.honhimw.spring.web.common.ExceptionWrapper;
import io.github.honhimw.spring.web.common.ExceptionWrappers;
import io.github.honhimw.spring.web.common.i18n.I18nUtils;
import io.github.honhimw.spring.web.mvc.FallbackHandlerExceptionResolver;
import io.github.honhimw.spring.web.mvc.MvcExtendConfig;
import io.github.honhimw.spring.web.mvc.MvcHealthyCheckEndpointFilter;
import io.github.honhimw.spring.web.mvc.MvcHttpLogFilter;
import io.github.honhimw.spring.web.reactive.*;
import io.github.honhimw.spring.web.reactive.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.http.codec.json.AbstractJackson2Encoder;
import org.springframework.http.server.reactive.HttpHandlerDecoratorFactory;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.*;

/**
 * @author hon_him
 * @since 2023-05-09
 */

abstract class WebConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ANY)
    @ComponentScan(basePackages = "io.github.honhimw.spring.web.common")
    static class IWebConfiguration {

        @Bean("exceptionWrappers")
        @ConditionalOnMissingBean(name = "exceptionWrappers")
        ExceptionWrappers exceptionWrappers(List<ExceptionWrapper> exceptionWrappers) {
            return new ExceptionWrappers(exceptionWrappers);
        }

        @Bean("i18NUtils")
        @ConditionalOnMissingBean(name = "i18NUtils")
        @ConditionalOnBean(MessageSource.class)
        I18nUtils i18NUtils() {
            return new I18nUtils();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = SERVLET)
    @ComponentScan(basePackages = "io.github.honhimw.spring.web.mvc")
    static class IMvcConfiguration {

        @Bean
        @ConditionalOnMissingBean(MvcExtendConfig.class)
        WebMvcConfigurer mvcExtendConfig() {
            return new MvcExtendConfig();
        }

        @Bean
        @ConditionalOnMissingBean(name = "fallbackHandlerExceptionResolver")
        HandlerExceptionResolver fallbackHandlerExceptionResolver(HttpMessageConverters converters, ExceptionWrappers wrappers, IWebProperties iWebProperties) {
            Boolean fallbackHandlerPrintStacktrace = iWebProperties.getFallbackHandlerPrintStacktrace();
            FallbackHandlerExceptionResolver fallbackHandlerExceptionResolver = new FallbackHandlerExceptionResolver(converters, wrappers);
            fallbackHandlerExceptionResolver.setPrintStacktrace(fallbackHandlerPrintStacktrace);
            return fallbackHandlerExceptionResolver;
        }

        @Bean
        @ConditionalOnMissingBean(name = "mvcHttpLogFilter")
        MvcHttpLogFilter mvcHttpLogFilter() {
            return new MvcHttpLogFilter();
        }

        @Bean
        @ConditionalOnMissingBean(MvcHealthyCheckEndpointFilter.class)
        @ConditionalOnProperty(name = "i.spring.web.healthy-check-point", havingValue = "true", matchIfMissing = true)
        MvcHealthyCheckEndpointFilter mvcHealthyCheckEndpointFilter() {
            return new MvcHealthyCheckEndpointFilter();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = REACTIVE)
    @ComponentScan(basePackages = "io.github.honhimw.spring.web.reactive")
    static class IWebFluxConfiguration {

        @Bean("webFluxJackson2Decoder")
        @ConditionalOnMissingBean(name = "webFluxJackson2Decoder")
        AbstractJackson2Decoder webFluxJackson2Decoder() {
            return new WebFluxJackson2Decoder();
        }

        @Bean("webFluxJackson2Encoder")
        @ConditionalOnMissingBean(name = "webFluxJackson2Encoder")
        AbstractJackson2Encoder webFluxJackson2Encoder() {
            return new FetcherJackson2Encoder();
        }

        @Bean("reactiveExtendConfig")
        @ConditionalOnMissingBean(ReactiveExtendConfig.class)
        WebFluxConfigurer reactiveExtendConfig(AbstractJackson2Decoder decoder, AbstractJackson2Encoder encoder) {
            return new ReactiveExtendConfig(decoder, encoder);
        }

        @Bean
        @ConditionalOnMissingBean(ErrorWebExceptionHandler.class)
        @ConditionalOnBean(AbstractJackson2Encoder.class)
        ErrorWebExceptionHandler fallbackErrorWebExceptionHandler(AbstractJackson2Encoder jackson2HttpMessageEncoder, ExceptionWrappers wrappers, IWebProperties iWebProperties) {
            Boolean fallbackHandlerPrintStacktrace = iWebProperties.getFallbackHandlerPrintStacktrace();
            FallbackErrorWebExceptionHandler fallbackErrorWebExceptionHandler = new FallbackErrorWebExceptionHandler(jackson2HttpMessageEncoder, wrappers);
            fallbackErrorWebExceptionHandler.setPrintStacktrace(fallbackHandlerPrintStacktrace);
            return fallbackErrorWebExceptionHandler;
        }

        @Bean("exchangeContextFilter")
        @ConditionalOnMissingBean(name = "exchangeContextFilter")
        @ConditionalOnProperty(value = "web.exchange.context-holder", havingValue = "true", matchIfMissing = true)
        ExchangeContextFilter exchangeContextFilter() {
            return new ExchangeContextFilter("I_EXCHANGE_CTX");
        }

        @Order(ReactiveHttpLogHandler.DEFAULT_HTTP_LOG_HANDLER_ORDERED)
        @Bean("reactiveHttpLogHandlerDecoratorFactory")
        @ConditionalOnMissingBean(name = "reactiveHttpLogHandlerDecoratorFactory")
        HttpHandlerDecoratorFactory reactiveHttpLogHandlerDecoratorFactory() {
            return ReactiveHttpLogHandler::new;
        }

        @Bean
        @ConditionalOnMissingBean(ReactiveHealthyCheckEndpointFilter.class)
        @ConditionalOnProperty(name = "i.spring.web.healthy-check-point", havingValue = "true", matchIfMissing = true)
        ReactiveHealthyCheckEndpointFilter reactiveHealthyCheckEndpointFilter() {
            return new ReactiveHealthyCheckEndpointFilter();
        }

        @Bean("autoInitializeListener")
        @ConditionalOnMissingBean(name = "autoInitializeListener")
        AutoInitializeListener autoInitializeListener() {
            return new AutoInitializeListener()
                .addTask("Reactor Thread Local Accessor Config", ReactorThreadLocalConfig::initialize);
        }
    }

}
