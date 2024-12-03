package io.github.honhimw.spring.extend;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.Executors;

/**
 * Do nothing.
 * <pre>
 * see src/main/java21/io/github/honhimw/spring/extend/BootstrapExecutor.java
 * </pre>
 *
 * @author hon_him
 * @since 2024-11-20
 */

public class BootstrapExecutor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(@Nonnull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        int cpuCore = Runtime.getRuntime().availableProcessors();
        beanFactory.setBootstrapExecutor(Executors.newFixedThreadPool(4 * cpuCore));
    }

}
