package io.github.honhimw.spring.web.common;

import io.github.honhimw.spring.web.common.openapi.FetcherOpenApiCustomizer;
import io.github.honhimw.spring.web.common.openapi.SecurityOpenApiCustomizer;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hon_him
 * @since 2025-03-26
 */

@ConditionalOnClass(OpenAPI.class)
@Configuration(proxyBeanMethods = false)
public class OpenApiCustomConfiguration {

    @Bean(value = "fetcherOpenApiCustomizer")
    @ConditionalOnMissingBean(name = "fetcherOpenApiCustomizer")
    FetcherOpenApiCustomizer fetcherOpenApiCustomizer() {
        return new FetcherOpenApiCustomizer();
    }

    @Bean(value = "securityOpenApiCustomizer")
    @ConditionalOnMissingBean(name = "securityOpenApiCustomizer")
    @ConditionalOnDefaultWebSecurity
    SecurityOpenApiCustomizer securityOpenApiCustomizer() {
        return new SecurityOpenApiCustomizer();
    }

}
