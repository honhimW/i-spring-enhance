package io.github.honhimw.spring.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.core.IResult;
import io.github.honhimw.spring.extend.AsyncBlockingExecutionConfig;
import io.github.honhimw.spring.web.common.ExceptionWrapper;
import io.github.honhimw.spring.web.common.ExceptionWrapperConfiguration;
import io.github.honhimw.spring.web.common.ExceptionWrappers;
import io.github.honhimw.spring.web.common.i18n.I18nUtils;
import io.github.honhimw.spring.web.common.openapi.SecurityOpenApiCustomizer;
import io.github.honhimw.spring.web.mvc.*;
import io.github.honhimw.spring.web.reactive.*;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.system.JavaVersion;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.DelegatingMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.http.codec.json.AbstractJackson2Encoder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.HttpHandlerDecoratorFactory;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
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
    @Import(ExceptionWrapperConfiguration.class)
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

        @Bean(value = "securityOpenApiCustomizer")
        @ConditionalOnMissingBean(name = "securityOpenApiCustomizer")
        @ConditionalOnClass(OpenAPI.class)
        @ConditionalOnDefaultWebSecurity
        SecurityOpenApiCustomizer securityOpenApiCustomizer() {
            return new SecurityOpenApiCustomizer();
        }

        @Bean("i18nUtils")
        @ConditionalOnMissingBean(name = "i18nUtils")
        @ConditionalOnProperty(value = "i.spring.web.i18n", havingValue = "true", matchIfMissing = true)
//        @ConditionalOnBean(MessageSource.class)
        I18nUtils i18nUtils(ObjectProvider<MessageSource> messageSourceProvider, ObjectProvider<MessageSourceProperties> propertiesProvider) {
            MessageSource messageSource = messageSourceProvider.getIfUnique();
            if (messageSource instanceof ResourceBundleMessageSource resourceBundle) {
                Set<String> basenameSet = resourceBundle.getBasenameSet();
                String[] newBaseNameSet = Stream.concat(basenameSet.stream(), Stream.of("i18n/enhance_embedded")).toArray(String[]::new);
                resourceBundle.setBasenames(newBaseNameSet);
            } else {
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
                if (messageSource instanceof DelegatingMessageSource emptyMessageSource && StringUtils.equalsIgnoreCase(String.valueOf(messageSource), "Empty MessageSource")) {
                    emptyMessageSource.setParentMessageSource(resourceBundleMessageSource);
                } else {
                    I18nUtils i18nUtils = new I18nUtils();
                    i18nUtils.setMessageSource(resourceBundleMessageSource);
                    return i18nUtils;
                }
            }
            return new I18nUtils();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = SERVLET)
    static class IMvcConfiguration {

        @Fallback
        @Bean("mvcJackson2HttpMessageConverter")
        @ConditionalOnMissingBean(name = "mvcJackson2HttpMessageConverter")
        MvcJackson2HttpMessageConverter mvcJackson2HttpMessageConverter(ObjectMapper objectMapper) {
            return new MvcJackson2HttpMessageConverter(objectMapper, MediaType.APPLICATION_JSON);
        }

        @DependsOn("mvcJackson2HttpMessageConverter")
        @Bean("mvcExtendConfig")
        @ConditionalOnMissingBean(name = "mvcExtendConfig")
        WebMvcConfigurer mvcExtendConfig(ObjectProvider<MvcJackson2HttpMessageConverter> mvcJackson2HttpMessageConverters) {
            return new WebMvcConfigurer() {
                @Override
                public void extendMessageConverters(@Nonnull List<HttpMessageConverter<?>> converters) {
                    for (int i = 0; i < converters.size(); i++) {
                        if (converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
                            converters.set(i, mvcJackson2HttpMessageConverters.getIfAvailable());
                            return;
                        }
                    }
                    converters.add(mvcJackson2HttpMessageConverters.getIfAvailable());
                }
            };
        }

        @Bean("mvcCorsConfig")
        @ConditionalOnMissingBean(name = "mvcCorsConfig")
        @ConditionalOnProperty(name = "i.spring.web.cors", havingValue = "true", matchIfMissing = true)
        WebMvcConfigurer mvcCorsConfig() {
            return new WebMvcConfigurer() {
                @Override
                public void addCorsMappings(@Nonnull CorsRegistry registry) {
                    registry
                        .addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods(HttpMethod.POST.name(), HttpMethod.GET.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name())
                        .allowedHeaders("*")
                        .allowCredentials(true);
                }
            };
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
    static class IWebFluxConfiguration {

        @Bean("webFluxJackson2Decoder")
        @ConditionalOnMissingBean(name = "webFluxJackson2Decoder")
        AbstractJackson2Decoder webFluxJackson2Decoder(ObjectMapper objectMapper) {
            return new WebFluxJackson2Decoder(objectMapper, MediaType.APPLICATION_JSON);
        }

        @Bean("webFluxJackson2Encoder")
        @ConditionalOnMissingBean(name = "webFluxJackson2Encoder")
        AbstractJackson2Encoder webFluxJackson2Encoder(ObjectMapper objectMapper) {
            return new WebFluxJackson2Encoder(objectMapper, MediaType.APPLICATION_JSON);
        }

        @Bean("reactiveExtendConfig")
        @ConditionalOnMissingBean(name = "reactiveExtendConfig")
        WebFluxConfigurer reactiveExtendConfig(AbstractJackson2Decoder decoder, AbstractJackson2Encoder encoder) {
            return new WebFluxConfigurer() {
                @Override
                public void configureHttpMessageCodecs(@Nonnull ServerCodecConfigurer configurer) {
                    ServerCodecConfigurer.ServerDefaultCodecs serverDefaultCodecs = configurer.defaultCodecs();
                    serverDefaultCodecs.jackson2JsonDecoder(decoder);
                    serverDefaultCodecs.jackson2JsonEncoder(encoder);
                }
            };
        }

        @Bean("reactiveCorsConfig")
        @ConditionalOnMissingBean(name = "reactiveCorsConfig")
        @ConditionalOnProperty(name = "i.spring.web.cors", havingValue = "true", matchIfMissing = true)
        WebFluxConfigurer reactiveCorsConfig() {
            return new WebFluxConfigurer() {
                @Override
                public void addCorsMappings(@Nonnull org.springframework.web.reactive.config.CorsRegistry registry) {
                    registry
                        .addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("POST", "GET", "PUT", "DELETE")
                        .allowedHeaders("*")
                        .allowCredentials(true);
                }
            };
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
