package io.github.honhimw.ddd.jimmer.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author hon_him
 * @since 2025-01-13
 */

@ConditionalOnProperty(
    prefix = "spring.data.jimmer.repositories",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@ConditionalOnMissingBean({
    JimmerRepositoryFactoryBean.class,
    JimmerRepositoryConfigExtension.class,
})
public class JimmerRepositoriesConfig {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(JimmerRepositoryConfigExtension.class)
    @Import(JimmerRepositoriesRegistrar.class)
    static class JimmerRepositoriesConfiguration {}
}
