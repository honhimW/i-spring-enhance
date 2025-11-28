package io.github.honhimw.spring.extend;

import io.github.honhimw.spring.event.ApplicationBeanReadyEvent;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author hon_him
 * @since 2024-11-21
 */

public class ApplicationPhaseExtendPublisher implements ApplicationContextAware, SmartInitializingSingleton {

    private ApplicationContext context;

    @Override
    public void afterSingletonsInstantiated() {
        context.publishEvent(new ApplicationBeanReadyEvent(context));
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
