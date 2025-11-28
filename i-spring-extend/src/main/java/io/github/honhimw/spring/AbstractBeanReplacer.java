package io.github.honhimw.spring;

import org.jspecify.annotations.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Replacing a specified bean through the bean name is a process that is commonly used for unit testing
 * or for specific behaviors in specific environments.
 * <p/>
 * <h2>Usage:</h2>
 * <pre>
 * &#64Bean
 * AbstractBeanReplacer<BuildIn> simpleBeanReplacer() {
 *     return new AbstractBeanReplacer<>("foo") {
 *         &#64Override
 *         protected Class<BuildIn> beanType() {
 *             return BuildIn.class;
 *         }
 *
 *         &#64Override
 *         protected Supplier<BuildIn> beanSupplier() {
 *             return () -> (BuildIn) () -> {
 *
 *             };
 *         }
 *     };
 * }
 * </pre>
 *
 * @author hon_him
 * @since 2023-11-27
 */

@Slf4j
public abstract class AbstractBeanReplacer<T> implements BeanDefinitionRegistryPostProcessor {

    /**
     * the very bean that would be replaced
     */
    protected final String beanName;

    public AbstractBeanReplacer(String beanName) {
        this.beanName = beanName;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    protected Class<T> beanType() {
        ParameterizedType parameterizedType =
            (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class<T>) actualTypeArguments[0];
    }

    /**
     * @see BeanDefinitionBuilder#genericBeanDefinition(Class) for returing null
     * @see BeanDefinitionBuilder#genericBeanDefinition(Class, Supplier) for returing not null
     * @return may supply a bean that was already constructed. anonymous inner class e.g.
     */
    @Nullable
    protected Supplier<T> beanSupplier() {
        return null;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry) throws BeansException {
        if (registry.containsBeanDefinition(beanName)) {
            log.warn("replacing found bean: [{}]", beanName);
            registry.removeBeanDefinition(beanName);
            Class<T> beanType = beanType();
            Supplier<T> beanSupplier = beanSupplier();
            AbstractBeanDefinition beanDefinition;
            if (Objects.nonNull(beanSupplier)) {
                beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(beanType, beanSupplier)
                    .getBeanDefinition();
            } else {
                beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(beanType)
                    .getBeanDefinition();
            }
            registry.registerBeanDefinition(beanName, beanDefinition);
        } else {
            log.warn("bean-replacer for [{}] not found", beanName);
        }
    }

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    public static <R> AbstractBeanReplacer<R> replaceWith(String beanName, R bean) {
        return new SimpleBeanReplacer<>(beanName, bean);
    }

    public static <R> AbstractBeanReplacer<R> replaceWith(String beanName, Supplier<R> supplier) {
        return new SimpleBeanReplacer<>(beanName, supplier);
    }

    private static final class SimpleBeanReplacer<T> extends AbstractBeanReplacer<T> {

        private final Supplier<T> supplier;

        private SimpleBeanReplacer(String beanName, Supplier<T> supplier) {
            super(beanName);
            this.supplier = supplier;
        }

        private SimpleBeanReplacer(String beanName, T bean) {
            super(beanName);
            this.supplier = () -> bean;
        }

        @Nullable
        @Override
        protected Supplier<T> beanSupplier() {
            return supplier;
        }
    }

}
