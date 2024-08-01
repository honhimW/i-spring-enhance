package io.github.honhimw.spring.web;

import io.github.honhimw.spring.web.common.ExceptionWrappers;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author hon_him
 * @since 2023-05-09
 */

@EnableConfigurationProperties(IWebProperties.class)
@ConditionalOnWebApplication
@ConditionalOnClass(ExceptionWrappers.class)
@AutoConfiguration(before = {ErrorWebFluxAutoConfiguration.class, ResolverAutoConfiguration.class})
@Configuration(proxyBeanMethods = false)
@Import({
    WebConfiguration.IWebConfiguration.class,
    WebConfiguration.IMvcConfiguration.class,
    WebConfiguration.IWebFluxConfiguration.class,
})
public class ISpringWebAutoConfiguration {

}
