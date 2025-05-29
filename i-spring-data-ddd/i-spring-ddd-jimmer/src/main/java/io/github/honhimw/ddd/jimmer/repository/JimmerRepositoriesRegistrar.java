package io.github.honhimw.ddd.jimmer.repository;

import io.github.honhimw.ddd.jimmer.EnableJimmerRepositories;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * @author hon_him
 * @since 2025-01-13
 */

public class JimmerRepositoriesRegistrar extends AbstractRepositoryConfigurationSourceSupport {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableJimmerRepositories.class;
    }

    @Override
    protected Class<?> getConfiguration() {
        return EnableJimmerRepositoriesConfiguration.class;
    }

    @Override
    protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        return new JimmerRepositoryConfigExtension();
    }

    @EnableJimmerRepositories
    private static class EnableJimmerRepositoriesConfiguration {}
}
