package io.github.honhimw.spring.extend;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import jakarta.annotation.Nonnull;
import java.util.concurrent.Executors;

/**
 * @author hon_him
 * @since 2024-11-20
 */

public class BootstrapExecutor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(@Nonnull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.setBootstrapExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }

}
