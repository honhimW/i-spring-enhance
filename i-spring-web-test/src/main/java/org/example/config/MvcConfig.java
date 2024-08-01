package org.example.config;

import io.github.honhimw.spring.util.JsonUtils;
import io.github.honhimw.spring.web.common.resolver.CsvJacksonNodeCustomizer;
import io.github.honhimw.spring.web.mvc.MvcLoggingRebinderEndpointFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author hon_him
 * @since 2023-05-22
 */

@Configuration
@EnableWebMvc
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class MvcConfig {

    @Bean
    MvcLoggingRebinderEndpointFilter mvcLoggingRebinderEndpointFilter() {
        return new MvcLoggingRebinderEndpointFilter();
    }

    @Bean
    CsvJacksonNodeCustomizer csvJacksonNodeCustomizer() {
        return new CsvJacksonNodeCustomizer(JsonUtils.getObjectMapper());
    }

}
