package io.github.honhimw.ddd.jimmer.repository;

import jakarta.annotation.Nonnull;
import org.babyfish.jimmer.sql.Entity;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author hon_him
 * @since 2025-01-13
 */

public class JimmerRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {

    @Nonnull
    @Override
    public String getModuleName() {
        return "Jimmer";
    }

    @Nonnull
    @Override
    public String getRepositoryFactoryBeanClassName() {
        return JimmerRepositoryFactoryBean.class.getName();
    }

    @Deprecated
    @Nonnull
    @Override
    protected String getModulePrefix() {
        return getModuleName().toLowerCase(Locale.US);
    }

    @Override
    public void postProcess(@Nonnull BeanDefinitionBuilder builder, RepositoryConfigurationSource source) {
        source.getAttribute("sqlClientRef") //
            .filter(StringUtils::hasText) //
            .ifPresent(s -> builder.addPropertyReference("sqlClient", s));
    }

    @Nonnull
    @Override
    protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
        return Collections.singleton(Entity.class);
    }

    @Nonnull
    @Override
    protected Collection<Class<?>> getIdentifyingTypes() {
        return List.of(JimmerRepository.class);
    }
}
