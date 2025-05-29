package io.github.honhimw.ddd.jimmer.repository;

import io.github.honhimw.ddd.jimmer.EnableJimmerRepositories;
import jakarta.annotation.Nonnull;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * @author hon_him
 * @since 2025-01-13
 */

public class JimmerRepositoriesRegistrarForAnnotation extends RepositoryBeanDefinitionRegistrarSupport {

    @Nonnull
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableJimmerRepositories.class;
    }

    @Nonnull
    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new JimmerRepositoryConfigExtension();
    }
}
