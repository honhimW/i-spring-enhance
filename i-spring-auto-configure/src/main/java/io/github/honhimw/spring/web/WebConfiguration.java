package io.github.honhimw.spring.web;

import io.github.honhimw.core.IResult;
import io.github.honhimw.spring.extend.AsyncBlockingExecutionConfig;
import io.github.honhimw.spring.web.common.ExceptionWrapper;
import io.github.honhimw.spring.web.common.ExceptionWrappers;
import io.github.honhimw.spring.web.common.i18n.I18nUtils;
import io.github.honhimw.spring.web.mvc.FallbackHandlerExceptionResolver;
import io.github.honhimw.spring.web.mvc.MvcHealthyCheckEndpointFilter;
import io.github.honhimw.spring.web.mvc.MvcHttpLogFilter;
import io.github.honhimw.spring.web.mvc.MvcTraceFilter;
import io.github.honhimw.spring.web.reactive.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.system.JavaVersion;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.DelegatingMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.http.codec.json.AbstractJackson2Encoder;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.HttpHandlerDecoratorFactory;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Stream;

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

        @Bean(value = "exceptionWrapperMessageFormatter")
        @ConditionalOnMissingBean(value = ExceptionWrapper.MessageFormatter.class)
        ExceptionWrapper.MessageFormatter exceptionWrapperMessageFormatter() {
            return IResult::of;
        }

        @Bean(value = "exceptionWrappers")
        @ConditionalOnMissingBean(name = "exceptionWrappers")
        ExceptionWrappers exceptionWrappers(ObjectProvider<ExceptionWrapper> exceptionWrappers) {
            return new ExceptionWrappers(exceptionWrappers);
        }

        @Bean("i18nUtils")
        @ConditionalOnMissingBean(name = "i18nUtils")
        @ConditionalOnProperty(value = "i.spring.web.i18n", havingValue = "true", matchIfMissing = true)
        @ConditionalOnBean(MessageSource.class)
        I18nUtils i18nUtils(ObjectProvider<MessageSource> messageSourceProvider, ObjectProvider<MessageSourceProperties> propertiesProvider) {
            MessageSource messageSource = messageSourceProvider.getIfUnique();
            if (messageSource instanceof ResourceBundleMessageSource resourceBundle) {
                Set<String> basenameSet = resourceBundle.getBasenameSet();
                String[] newBaseNameSet = Stream.concat(basenameSet.stream(), Stream.of("i18n/enhance_embedded")).toArray(String[]::new);
                resourceBundle.setBasenames(newBaseNameSet);
            } else if (messageSource instanceof DelegatingMessageSource emptyMessageSource && StringUtils.equalsIgnoreCase(String.valueOf(messageSource), "Empty MessageSource")) {
                MessageSourceProperties properties = propertiesProvider.getIfAvailable(MessageSourceProperties::new);
                ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
                resourceBundleMessageSource.setBasename("i18n/enhance_embedded");
                if (properties.getEncoding() != null) {
                    resourceBundleMessageSource.setDefaultEncoding(properties.getEncoding().name());
                }
                resourceBundleMessageSource.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
                Duration cacheDuration = properties.getCacheDuration();
                if (cacheDuration != null) {
                    resourceBundleMessageSource.setCacheMillis(cacheDuration.toMillis());
                }
                resourceBundleMessageSource.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());
                resourceBundleMessageSource.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
                emptyMessageSource.setParentMessageSource(resourceBundleMessageSource);
            }
            return new I18nUtils();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = SERVLET)
    @ComponentScan(basePackages = "io.github.honhimw.spring.web.mvc")
    static class IMvcConfiguration {

        @Bean("mvcExtendConfig")
        @ConditionalOnMissingBean(name = "mvcExtendConfig")
        WebMvcConfigurer mvcExtendConfig() {
            return new MvcExtendConfig();
        }

        @Bean("fallbackHandlerExceptionResolver")
        @ConditionalOnMissingBean(name = "fallbackHandlerExceptionResolver")
        HandlerExceptionResolver fallbackHandlerExceptionResolver(
            HttpMessageConverters converters,
            ExceptionWrappers wrappers,
            IWebProperties iWebProperties,
            ExceptionWrapper.MessageFormatter messageFormatter) {
            Boolean fallbackHandlerPrintStacktrace = iWebProperties.getFallbackHandlerPrintStacktrace();
            FallbackHandlerExceptionResolver fallbackHandlerExceptionResolver = new FallbackHandlerExceptionResolver(converters, wrappers, messageFormatter);
            fallbackHandlerExceptionResolver.setPrintStacktrace(fallbackHandlerPrintStacktrace);
            return fallbackHandlerExceptionResolver;
        }

        @Bean("mvcHttpLogFilter")
        @ConditionalOnMissingBean(name = "mvcHttpLogFilter")
        MvcHttpLogFilter mvcHttpLogFilter() {
            return new MvcHttpLogFilter();
        }

        @Bean("mvcTraceFilter")
        @ConditionalOnProperty(name = "i.spring.web.trace.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = "mvcTraceFilter")
        MvcTraceFilter mvcTraceFilter(IWebProperties iWebProperties) {
            return new MvcTraceFilter(
                iWebProperties.getTrace().getLength(),
                iWebProperties.getTrace().getTraceHeader(),
                iWebProperties.getTrace().getTraceKey()
            );
        }

        @Bean("mvcHealthyCheckEndpointFilter")
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
            return new WebFluxJackson2Encoder();
        }

        @Bean("reactiveExtendConfig")
        @ConditionalOnMissingBean(ReactiveExtendConfig.class)
        WebFluxConfigurer reactiveExtendConfig(AbstractJackson2Decoder decoder, AbstractJackson2Encoder encoder) {
            return new ReactiveExtendConfig(decoder, encoder);
        }

        @Bean("asyncBlockingExecutionConfig")
        @ConditionalOnMissingBean(name = "asyncBlockingExecutionConfig")
        @ConditionalOnJava(JavaVersion.TWENTY_ONE)
        @ConditionalOnProperty(value = "spring.threads.virtual.enabled", havingValue = "true")
        WebFluxConfigurer asyncBlockingExecutionConfig() {
            return new AsyncBlockingExecutionConfig();
        }

        @Bean("fallbackErrorWebExceptionHandler")
        @ConditionalOnMissingBean(ErrorWebExceptionHandler.class)
        @ConditionalOnBean(AbstractJackson2Encoder.class)
        ErrorWebExceptionHandler fallbackErrorWebExceptionHandler(
            AbstractJackson2Encoder jackson2HttpMessageEncoder,
            ExceptionWrappers wrappers,
            IWebProperties iWebProperties,
            ExceptionWrapper.MessageFormatter messageFormatter) {
            Boolean fallbackHandlerPrintStacktrace = iWebProperties.getFallbackHandlerPrintStacktrace();
            FallbackErrorWebExceptionHandler fallbackErrorWebExceptionHandler = new FallbackErrorWebExceptionHandler(jackson2HttpMessageEncoder, wrappers, messageFormatter);
            fallbackErrorWebExceptionHandler.setPrintStacktrace(fallbackHandlerPrintStacktrace);
            return fallbackErrorWebExceptionHandler;
        }

        @Bean(value = "exchangeContextFilter")
        @ConditionalOnClass(io.micrometer.context.ThreadLocalAccessor.class)
        @ConditionalOnMissingBean(name = "exchangeContextFilter")
        @ConditionalOnProperty(value = "i.spring.web.exchange.context-holder", havingValue = "true", matchIfMissing = true)
        ExchangeContextFilter exchangeContextFilter() {
            return new ExchangeContextFilter("I_EXCHANGE_CTX");
        }

        @Order(ReactiveHttpLogHandler.DEFAULT_HANDLER_ORDERED)
        @Bean(value = "reactiveHttpLogHandlerDecoratorFactory")
        @ConditionalOnMissingBean(name = "reactiveHttpLogHandlerDecoratorFactory")
        HttpHandlerDecoratorFactory reactiveHttpLogHandlerDecoratorFactory() {
            return ReactiveHttpLogHandler::new;
        }

        @Order(ReactiveTraceHandler.DEFAULT_HANDLER_ORDERED)
        @Bean(value = "reactiveTraceHandlerDecoratorFactory")
        @ConditionalOnProperty(name = "i.spring.web.trace.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = "reactiveTraceHandlerDecoratorFactory")
        HttpHandlerDecoratorFactory reactiveTraceHandlerDecoratorFactory(IWebProperties iWebProperties) {
            return httpHandler -> new ReactiveTraceHandler(
                httpHandler,
                iWebProperties.getTrace().getReactorContextKey(),
                iWebProperties.getTrace().getLength(),
                iWebProperties.getTrace().getTraceHeader(),
                iWebProperties.getTrace().getTraceKey()
            );
        }

        @Order(ReactiveTraceHandler.DEFAULT_HANDLER_ORDERED + 1000)
        @Bean(value = "reactiveSchedulerHandlerDecoratorFactory")
        @ConditionalOnProperty(name = "i.spring.web.reactive.force-scheduler", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = "reactiveSchedulerHandlerDecoratorFactory")
        HttpHandlerDecoratorFactory reactiveSchedulerHandlerDecoratorFactory() {
            return httpHandler ->
                (HttpHandler) (request, response) ->
                    httpHandler.handle(request, response).subscribeOn(Schedulers.boundedElastic());
        }

        @Bean(value = "reactiveHealthyCheckEndpointFilter")
        @ConditionalOnMissingBean(ReactiveHealthyCheckEndpointFilter.class)
        @ConditionalOnProperty(name = "i.spring.web.healthy-check-point", havingValue = "true", matchIfMissing = true)
        ReactiveHealthyCheckEndpointFilter reactiveHealthyCheckEndpointFilter() {
            return new ReactiveHealthyCheckEndpointFilter();
        }

        @Bean(value = "autoInitializer")
        @ConditionalOnClass(io.micrometer.context.ThreadLocalAccessor.class)
        @ConditionalOnMissingBean(name = "autoInitializer")
        AutoInitializer autoInitializer() {
            return new AutoInitializer()
                .addTask("Reactor Thread Local Accessor Config", ReactorThreadLocalConfig::initialize);
        }
    }

}
