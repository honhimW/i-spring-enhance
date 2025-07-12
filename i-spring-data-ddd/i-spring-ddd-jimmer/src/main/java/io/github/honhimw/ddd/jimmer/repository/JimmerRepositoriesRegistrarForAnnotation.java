package io.github.honhimw.ddd.jimmer.repository;

import io.github.honhimw.ddd.jimmer.EnableJimmerRepositories;
import jakarta.annotation.Nonnull;
import org.babyfish.jimmer.meta.ImmutableType;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.repository.config.*;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

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
//
//    private ResourceLoader resourceLoader;
//    private Environment environment;
//
//    @Override
//    public void setResourceLoader(@Nonnull ResourceLoader resourceLoader) {
//        super.setResourceLoader(resourceLoader);
//        this.resourceLoader = resourceLoader;
//    }
//
//    @Override
//    public void setEnvironment(@Nonnull Environment environment) {
//        super.setEnvironment(environment);
//        this.environment = environment;
//    }
//
//    @Override
//    public void registerBeanDefinitions(@Nonnull AnnotationMetadata metadata, @Nonnull BeanDefinitionRegistry registry, @Nonnull BeanNameGenerator generator) {
//        Assert.notNull(metadata, "AnnotationMetadata must not be null");
//        Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
//        Assert.notNull(resourceLoader, "ResourceLoader must not be null");
//
//        // Guard against calls for sub-classes
//        if (metadata.getAnnotationAttributes(getAnnotation().getName()) == null) {
//            return;
//        }
//
//        AnnotationRepositoryConfigurationSource configurationSource = new AnnotationRepositoryConfigurationSource(metadata,
//            getAnnotation(), resourceLoader, environment, registry, generator);
//
//        RepositoryConfigurationExtension extension = getExtension();
//        RepositoryConfigurationUtils.exposeRegistration(extension, registry, configurationSource);
//
//        RepositoryConfigurationDelegate delegate = new RepositoryConfigurationDelegate(configurationSource, resourceLoader,
//            environment);
//
//        List<BeanComponentDefinition> beanComponentDefinitions = delegate.registerRepositoriesIn(registry, extension);
//        List<ImmutableType> immutableTypes = new ArrayList<>(beanComponentDefinitions.size());
//        for (BeanComponentDefinition beanComponentDefinition : beanComponentDefinitions) {
//            Class<?> entityType = beanComponentDefinition.getBeanDefinition().getResolvableType().getGeneric(1).resolve();
//            ImmutableType immutableType = ImmutableType.tryGet(entityType);
//            if (immutableType != null) {
//                immutableTypes.add(immutableType);
//            }
//        }
//        registry.registerBeanDefinition("", BeanDefinitionBuilder.().getBeanDefinition());
//    }

}
